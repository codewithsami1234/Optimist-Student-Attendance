package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.utils.PreferenceManager;

public class RoomAttendanceRepository implements AttendanceRepository {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomAttendanceRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void saveSession(String departmentId, String departmentName, String semesterId,
                             String semesterName, String sectionId, String sectionName,
                             String subjectId, String subjectName, String date, int lectureNumber,
                             Map<String, AttendanceStatus> studentIdToStatus,
                             Map<String, String> studentIdToName,
                             Map<String, String> studentIdToRoll,
                             ApiCallback<AttendanceSession> callback) {
        LocalExec.io(() -> {
            if (sectionId == null || sectionId.isEmpty()) {
                throw new LocalDataException("Please select a section first.");
            }
            if (studentIdToStatus == null || studentIdToStatus.isEmpty()) {
                throw new LocalDataException("There are no students to save attendance for.");
            }
            for (AttendanceStatus status : studentIdToStatus.values()) {
                if (status == null || status == AttendanceStatus.NOT_MARKED) {
                    throw new LocalDataException("Please mark attendance for every student before saving.");
                }
            }
            if (date == null || date.isEmpty()) {
                throw new LocalDataException("Please select a date.");
            }
            if (RoomAnalyticsRepository.isFutureDate(date)) {
                throw new LocalDataException("Attendance cannot be marked for a future date.");
            }

            String ownerId = LocalOwner.requireId(prefs);
            User teacher = LocalOwner.requireUser(prefs);
            return db.runInTransaction((java.util.concurrent.Callable<AttendanceSession>) () -> {
            SectionEntity section = db.sectionDao().findOwned(sectionId, ownerId);
            if (section == null) {
                throw new LocalDataException("Section not found.");
            }
            SemesterEntity semester = db.semesterDao().findOwned(section.semesterId, ownerId);
            if (semester == null) {
                throw new LocalDataException("Semester not found.");
            }
            SubjectEntity subject = resolveSubject(ownerId, semester.departmentId, subjectId);

            List<StudentEntity> roster = db.studentDao().listBySection(ownerId, sectionId);
            if (roster.isEmpty()) {
                throw new LocalDataException("There are no students to save attendance for.");
            }
            Set<String> rosterIds = new HashSet<>();
            for (StudentEntity student : roster) {
                rosterIds.add(student.id);
            }
            if (studentIdToStatus.size() != rosterIds.size()) {
                throw new LocalDataException("Please mark attendance for every student before saving.");
            }
            for (String studentKey : studentIdToStatus.keySet()) {
                if (!rosterIds.contains(studentKey)) {
                    throw new LocalDataException("One or more students are not in this section.");
                }
            }

            String sessionId = section.id + "_" + subject.id + "_" + date;
            List<AttendanceSessionEntity> existingSessions = db.attendanceSessionDao()
                    .listByCourseDate(ownerId, section.id, subject.id, date);
            for (AttendanceSessionEntity existing : existingSessions) {
                db.attendanceRecordDao().deleteBySession(existing.id, ownerId);
                db.attendanceSessionDao().delete(existing.id, ownerId);
            }
            if (db.attendanceSessionDao().findOwned(sessionId, ownerId) != null) {
                db.attendanceRecordDao().deleteBySession(sessionId, ownerId);
                db.attendanceSessionDao().delete(sessionId, ownerId);
            }

            AttendanceSessionEntity session = new AttendanceSessionEntity();
            session.id = sessionId;
            session.ownerUserId = ownerId;
            session.departmentId = semester.departmentId;
            session.departmentName = departmentName != null ? departmentName : "";
            session.semesterId = semester.id;
            session.semesterName = semesterName != null ? semesterName : semester.name;
            session.sectionId = section.id;
            session.sectionName = sectionName != null ? sectionName : section.name;
            session.subjectId = subject.id;
            session.subjectName = subjectName != null ? subjectName : subject.name;
            session.date = date;
            session.lectureNumber = 1;
            session.createdAtMillis = System.currentTimeMillis();
            db.attendanceSessionDao().insert(session);

            List<AttendanceRecordEntity> records = new ArrayList<>();
            for (StudentEntity student : roster) {
                AttendanceRecordEntity record = new AttendanceRecordEntity();
                record.id = sessionId + "_" + student.id;
                record.ownerUserId = ownerId;
                record.sessionId = sessionId;
                record.studentId = student.id;
                record.studentName = student.name;
                record.rollNumber = student.rollNumber;
                record.departmentId = semester.departmentId;
                record.semesterId = semester.id;
                record.sectionId = section.id;
                record.date = date;
                record.status = studentIdToStatus.get(student.id);
                record.markedBy = teacher.getName();
                record.markedAtMillis = System.currentTimeMillis();
                records.add(record);
            }
            db.attendanceRecordDao().insertAll(records);

            AttendanceSession saved = hydrate(session, records);
            notifyAttendanceSaved(ownerId, saved);
            java.util.List<String> studentIds = new java.util.ArrayList<>();
            for (AttendanceRecord record : saved.getRecords()) {
                studentIds.add(record.getStudentId());
            }
            com.example.studentattendance.sync.LowAttendanceEvaluator.notifyIfBelow(
                    db, ownerId, teacher, studentIds);
            return saved;
            });
        }, callback);
    }

    @Override
    public void getSession(String departmentId, String semesterId, String sectionId, String subjectId,
                             String date, int lectureNumber,
                             ApiCallback<AttendanceSession> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            SectionEntity section = db.sectionDao().findOwned(sectionId, ownerId);
            if (section == null) {
                throw new LocalDataException("Section not found.");
            }
            if (subjectId == null || subjectId.isEmpty()) {
                return null;
            }
            AttendanceSessionEntity entity = db.attendanceSessionDao().findByCourseDate(
                    ownerId, sectionId, subjectId, date);
            if (entity == null) {
                return null;
            }
            return hydrate(entity, db.attendanceRecordDao().listBySession(entity.id, ownerId));
        }, callback);
    }

    @Override
    public void getSessions(String departmentIdOrNull, String semesterIdOrNull, String sectionIdOrNull,
                             String subjectIdOrNull, String fromDateOrNull, String toDateOrNull,
                             ApiCallback<List<AttendanceSession>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<AttendanceSession> result = new ArrayList<>();
            for (AttendanceSessionEntity entity : db.attendanceSessionDao().listAll(ownerId)) {
                if (notBlank(departmentIdOrNull) && !departmentIdOrNull.equals(entity.departmentId)) {
                    continue;
                }
                if (notBlank(semesterIdOrNull) && !semesterIdOrNull.equals(entity.semesterId)) {
                    continue;
                }
                if (notBlank(sectionIdOrNull) && !sectionIdOrNull.equals(entity.sectionId)) {
                    continue;
                }
                if (notBlank(subjectIdOrNull) && !subjectIdOrNull.equals(entity.subjectId)) {
                    continue;
                }
                if (notBlank(fromDateOrNull) && entity.date.compareTo(fromDateOrNull) < 0) {
                    continue;
                }
                if (notBlank(toDateOrNull) && entity.date.compareTo(toDateOrNull) > 0) {
                    continue;
                }
                result.add(hydrate(entity, db.attendanceRecordDao().listBySession(entity.id, ownerId)));
            }
            Collections.sort(result, (a, b) -> {
                int dateCmp = b.getDate().compareTo(a.getDate());
                if (dateCmp != 0) {
                    return dateCmp;
                }
                return Integer.compare(a.getLectureNumber(), b.getLectureNumber());
            });
            return result;
        }, callback);
    }

    @Override
    public void importAttendance(String departmentId, String semesterId, String sectionId,
                                 String subjectId,
                                 java.util.List<com.example.studentattendance.utils.AttendanceImportParser.Row> rows,
                                 ApiCallback<com.example.studentattendance.model.ImportResult> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            User teacher = LocalOwner.requireUser(prefs);
            return db.runInTransaction((java.util.concurrent.Callable<com.example.studentattendance.model.ImportResult>) () -> {
                com.example.studentattendance.model.ImportResult result =
                        new com.example.studentattendance.model.ImportResult();
                if (rows == null || rows.isEmpty()) {
                    return result;
                }
                SectionEntity section = db.sectionDao().findOwned(sectionId, ownerId);
                if (section == null) {
                    throw new LocalDataException("Section not found.");
                }
                SemesterEntity semester = db.semesterDao().findOwned(section.semesterId, ownerId);
                if (semester == null) {
                    throw new LocalDataException("Semester not found.");
                }
                SubjectEntity subject = resolveSubject(ownerId, semester.departmentId, subjectId);
                if (departmentId != null && !departmentId.equals(semester.departmentId)) {
                    throw new LocalDataException("That class does not belong to this department.");
                }
                if (semesterId != null && !semesterId.equals(semester.id)) {
                    throw new LocalDataException("That class does not belong to this semester.");
                }
                java.util.Map<String, StudentEntity> byRoll = new java.util.HashMap<>();
                for (StudentEntity student : db.studentDao().listBySection(ownerId, section.id)) {
                    if (student.rollNumber != null) {
                        byRoll.put(student.rollNumber.trim().toLowerCase(java.util.Locale.US), student);
                    }
                }
                java.util.Map<String, AttendanceSessionEntity> sessionsByDate = new java.util.HashMap<>();
                for (com.example.studentattendance.utils.AttendanceImportParser.Row row : rows) {
                    if (row == null) {
                        continue;
                    }
                    if (row.courseCode != null && subject.code != null
                            && !row.courseCode.equalsIgnoreCase(subject.code)) {
                        result.setInvalid(result.getInvalid() + 1);
                        result.getErrors().add(new com.example.studentattendance.model.ImportRowError(
                                row.rowNumber, "Unknown course/class."));
                        continue;
                    }
                    if (RoomAnalyticsRepository.isFutureDate(row.date)) {
                        result.setInvalid(result.getInvalid() + 1);
                        result.getErrors().add(new com.example.studentattendance.model.ImportRowError(
                                row.rowNumber, "Invalid future date " + row.date + "."));
                        continue;
                    }
                    StudentEntity student = byRoll.get(row.rollNumber.trim().toLowerCase(java.util.Locale.US));
                    if (student == null) {
                        result.setInvalid(result.getInvalid() + 1);
                        result.getErrors().add(new com.example.studentattendance.model.ImportRowError(
                                row.rowNumber, "Unknown student roll " + row.rollNumber + "."));
                        continue;
                    }
                    AttendanceSessionEntity session = sessionsByDate.get(row.date);
                    if (session == null) {
                        session = db.attendanceSessionDao().findByCourseDate(ownerId, section.id, subject.id, row.date);
                        if (session == null) {
                            session = new AttendanceSessionEntity();
                            session.id = section.id + "_" + subject.id + "_" + row.date;
                            session.ownerUserId = ownerId;
                            session.departmentId = semester.departmentId;
                            session.departmentName = "";
                            session.semesterId = semester.id;
                            session.semesterName = semester.name;
                            session.sectionId = section.id;
                            session.sectionName = section.name;
                            session.subjectId = subject.id;
                            session.subjectName = subject.name;
                            session.date = row.date;
                            session.lectureNumber = 1;
                            session.createdAtMillis = System.currentTimeMillis();
                            db.attendanceSessionDao().insert(session);
                        }
                        sessionsByDate.put(row.date, session);
                    }
                    boolean exists = false;
                    for (AttendanceRecordEntity existing : db.attendanceRecordDao().listBySession(session.id, ownerId)) {
                        if (student.id.equals(existing.studentId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        result.setDuplicates(result.getDuplicates() + 1);
                        result.getErrors().add(new com.example.studentattendance.model.ImportRowError(
                                row.rowNumber, "Skipped duplicate attendance for " + row.rollNumber + "."));
                        continue;
                    }
                    AttendanceRecordEntity record = new AttendanceRecordEntity();
                    record.id = session.id + "_" + student.id;
                    record.ownerUserId = ownerId;
                    record.sessionId = session.id;
                    record.studentId = student.id;
                    record.studentName = student.name;
                    record.rollNumber = student.rollNumber;
                    record.departmentId = semester.departmentId;
                    record.semesterId = semester.id;
                    record.sectionId = section.id;
                    record.date = row.date;
                    record.status = row.status;
                    record.markedBy = teacher.getName();
                    record.markedAtMillis = System.currentTimeMillis();
                    db.attendanceRecordDao().insertAll(java.util.Collections.singletonList(record));
                    result.setImported(result.getImported() + 1);
                }
                return result;
            });
        }, callback);
    }

    @Override
    public void getSessionsForReports(ApiCallback<List<AttendanceSession>> callback) {
        getSessions(null, null, null, null, null, null, callback);
    }

    private SubjectEntity resolveSubject(String ownerId, String departmentId, String subjectId) {
        if (subjectId == null || subjectId.isEmpty()) {
            throw new LocalDataException("Please select a course before saving attendance.");
        }
        SubjectEntity subject = db.subjectDao().findOwned(subjectId, ownerId);
        if (subject == null) {
            throw new LocalDataException("Course not found.");
        }
        if (!departmentId.equals(subject.departmentId)) {
            throw new LocalDataException("That course does not belong to this department.");
        }
        if (subject.archived) {
            throw new LocalDataException("That course has been archived.");
        }
        return subject;
    }

    private AttendanceSession hydrate(AttendanceSessionEntity entity, List<AttendanceRecordEntity> records) {
        AttendanceSession session = entity.toModel();
        if (entity.subjectId != null && !entity.subjectId.isEmpty()) {
            SubjectEntity subject = db.subjectDao().findOwned(entity.subjectId, entity.ownerUserId);
            if (subject != null) {
                session.setSubjectCode(subject.code);
                if (session.getSubjectName() == null || session.getSubjectName().isEmpty()) {
                    session.setSubjectName(subject.name);
                }
            }
        }
        List<AttendanceRecord> mapped = new ArrayList<>();
        for (AttendanceRecordEntity record : records) {
            mapped.add(record.toModel());
        }
        session.setRecords(mapped);
        return session;
    }

    private void notifyAttendanceSaved(String ownerId, AttendanceSession session) {
        int present = 0;
        int absent = 0;
        int late = 0;
        int leave = 0;
        for (AttendanceRecord record : session.getRecords()) {
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                present++;
            } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                absent++;
            } else if (record.getStatus() == AttendanceStatus.LATE) {
                late++;
            } else if (record.getStatus() == AttendanceStatus.LEAVE) {
                leave++;
            }
        }
        NotificationEntity notification = new NotificationEntity();
        notification.id = "n_" + UUID.randomUUID();
        notification.ownerUserId = ownerId;
        notification.type = "ATTENDANCE_RECORDED";
        notification.message = String.format(
                "Attendance saved for %s on %s: %d present, %d absent, %d late, %d leave.",
                session.getSubjectName() != null ? session.getSubjectName() : "class",
                session.getDate(), present, absent, late, leave);
        notification.createdAtMillis = System.currentTimeMillis();
        db.notificationDao().insert(notification);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isEmpty();
    }
}
