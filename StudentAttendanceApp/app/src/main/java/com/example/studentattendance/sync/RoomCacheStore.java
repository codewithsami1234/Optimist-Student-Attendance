package com.example.studentattendance.sync;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.AdjustmentEntity;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.utils.PreferenceManager;

/** Writes server payloads into Room without changing the schema. */
public final class RoomCacheStore {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomCacheStore(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    public String ownerId() {
        return LocalOwner.requireId(prefs);
    }

    public void upsertDepartment(Department department) {
        if (department == null || department.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        DepartmentEntity entity = db.departmentDao().findOwned(department.getId(), ownerId);
        if (entity == null) {
            entity = new DepartmentEntity();
            entity.id = department.getId();
            entity.ownerUserId = ownerId;
        }
        entity.name = department.getName();
        entity.code = department.getCode();
        entity.headOfDepartment = department.getHeadOfDepartment();
        entity.shift = department.getShift();
        db.departmentDao().upsert(entity);
    }

    public void upsertSemester(Semester semester) {
        if (semester == null || semester.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        SemesterEntity entity = db.semesterDao().findOwned(semester.getId(), ownerId);
        if (entity == null) {
            entity = new SemesterEntity();
            entity.id = semester.getId();
            entity.ownerUserId = ownerId;
        }
        entity.departmentId = semester.getDepartmentId();
        entity.name = semester.getName();
        entity.semesterNumber = semester.getSemesterNumber();
        db.semesterDao().upsert(entity);
    }

    public void upsertSection(Section section) {
        if (section == null || section.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        SectionEntity entity = db.sectionDao().findOwned(section.getId(), ownerId);
        if (entity == null) {
            entity = new SectionEntity();
            entity.id = section.getId();
            entity.ownerUserId = ownerId;
        }
        entity.semesterId = section.getSemesterId();
        entity.name = section.getName();
        db.sectionDao().upsert(entity);
    }

    public void upsertSubject(Subject subject) {
        if (subject == null || subject.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        SubjectEntity entity = db.subjectDao().findOwned(subject.getId(), ownerId);
        if (entity == null) {
            entity = new SubjectEntity();
            entity.id = subject.getId();
            entity.ownerUserId = ownerId;
        }
        entity.departmentId = subject.getDepartmentId();
        entity.semesterId = subject.getSemesterId();
        entity.sectionId = subject.getSectionId();
        entity.name = subject.getName();
        entity.code = subject.getCode();
        entity.archived = subject.isArchived();
        db.subjectDao().upsert(entity);
    }

    public void upsertStudent(Student student) {
        if (student == null || student.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        StudentEntity entity = db.studentDao().findOwned(student.getId(), ownerId);
        if (entity == null) {
            entity = new StudentEntity();
            entity.id = student.getId();
            entity.ownerUserId = ownerId;
        }
        entity.rollNumber = student.getRollNumber();
        entity.name = student.getName();
        entity.departmentId = student.getDepartmentId();
        entity.semesterId = student.getSemesterId();
        entity.sectionId = student.getSectionId();
        entity.email = student.getEmail();
        entity.phone = student.getPhone();
        entity.archived = student.isArchived();
        db.studentDao().upsert(entity);
    }

    public void upsertSession(AttendanceSession session) {
        if (session == null || session.getId() == null) {
            return;
        }
        String ownerId = ownerId();
        AttendanceSessionEntity existing = db.attendanceSessionDao().findOwned(session.getId(), ownerId);
        if (existing != null && session.getRecords() != null && !session.getRecords().isEmpty()) {
            db.attendanceRecordDao().deleteBySession(existing.id, ownerId);
        }
        AttendanceSessionEntity entity = new AttendanceSessionEntity();
        entity.id = session.getId();
        entity.ownerUserId = ownerId;
        entity.departmentId = session.getDepartmentId();
        entity.departmentName = session.getDepartmentName();
        entity.semesterId = session.getSemesterId();
        entity.semesterName = session.getSemesterName();
        entity.sectionId = session.getSectionId();
        entity.sectionName = session.getSectionName();
        entity.subjectId = session.getSubjectId();
        entity.subjectName = session.getSubjectName();
        entity.date = session.getDate();
        entity.lectureNumber = session.getLectureNumber();
        entity.createdAtMillis = session.getCreatedAtMillis();
        db.attendanceSessionDao().insert(entity);

        if (session.getRecords() == null) {
            return;
        }
        List<AttendanceRecordEntity> records = new ArrayList<>();
        for (AttendanceRecord record : session.getRecords()) {
            AttendanceRecordEntity row = new AttendanceRecordEntity();
            row.id = record.getId() != null ? record.getId() : session.getId() + "_" + record.getStudentId();
            row.ownerUserId = ownerId;
            row.sessionId = session.getId();
            row.studentId = record.getStudentId();
            row.studentName = record.getStudentName();
            row.rollNumber = record.getRollNumber();
            row.departmentId = record.getDepartmentId();
            row.semesterId = record.getSemesterId();
            row.sectionId = record.getSectionId();
            row.date = record.getDate();
            row.status = record.getStatus();
            row.markedBy = record.getMarkedBy();
            row.markedAtMillis = record.getMarkedAtMillis();
            records.add(row);
        }
        if (!records.isEmpty()) {
            db.attendanceRecordDao().insertAll(records);
        }
    }

    public void upsertAdjustment(AttendanceAdjustment adjustment) {
        if (adjustment == null || adjustment.getId() == null) {
            return;
        }
        AdjustmentEntity entity = new AdjustmentEntity();
        entity.id = adjustment.getId();
        entity.ownerUserId = ownerId();
        entity.studentId = adjustment.getStudentId();
        entity.departmentId = adjustment.getDepartmentId();
        entity.delta = adjustment.getDelta();
        entity.reason = adjustment.getReason();
        entity.adjustedByUserId = adjustment.getAdjustedByUserId();
        entity.adjustedByName = adjustment.getAdjustedByName();
        entity.createdAtMillis = adjustment.getCreatedAtMillis();
        db.adjustmentDao().upsert(entity);
    }

    public void upsertNotifications(List<Notification> notifications) {
        if (notifications == null) {
            return;
        }
        String ownerId = ownerId();
        for (Notification notification : notifications) {
            if (notification == null || notification.getId() == null) {
                continue;
            }
            NotificationEntity entity = new NotificationEntity();
            entity.id = notification.getId();
            entity.ownerUserId = notification.getUserId() != null ? notification.getUserId() : ownerId;
            entity.type = notification.getType();
            entity.message = notification.getMessage();
            entity.relatedStudentId = notification.getRelatedStudentId();
            entity.read = notification.isRead();
            entity.createdAtMillis = notification.getCreatedAtMillis();
            db.notificationDao().upsert(entity);
        }
    }

    public void remapDepartmentId(String localId, String serverId) {
        if (skipRemap(localId, serverId)) {
            return;
        }
        String ownerId = ownerId();
        DepartmentEntity local = db.departmentDao().findOwned(localId, ownerId);
        if (local != null) {
            local.id = serverId;
            db.departmentDao().upsert(local);
            db.departmentDao().delete(localId, ownerId);
        }
        db.semesterDao().reassignDepartment(localId, serverId, ownerId);
        db.subjectDao().reassignDepartment(localId, serverId, ownerId);
        db.studentDao().reassignDepartment(localId, serverId, ownerId);
        db.attendanceSessionDao().reassignDepartment(localId, serverId, ownerId);
        db.attendanceRecordDao().reassignDepartment(localId, serverId, ownerId);
    }

    public void remapSemesterId(String localId, String serverId) {
        if (skipRemap(localId, serverId)) {
            return;
        }
        String ownerId = ownerId();
        SemesterEntity local = db.semesterDao().findOwned(localId, ownerId);
        if (local != null) {
            local.id = serverId;
            db.semesterDao().upsert(local);
            db.semesterDao().delete(localId, ownerId);
        }
        db.sectionDao().reassignSemester(localId, serverId, ownerId);
        db.studentDao().reassignSemester(localId, serverId, ownerId);
        db.attendanceSessionDao().reassignSemester(localId, serverId, ownerId);
        db.attendanceRecordDao().reassignSemester(localId, serverId, ownerId);
    }

    public void remapSectionId(String localId, String serverId) {
        if (skipRemap(localId, serverId)) {
            return;
        }
        String ownerId = ownerId();
        SectionEntity local = db.sectionDao().findOwned(localId, ownerId);
        if (local != null) {
            local.id = serverId;
            db.sectionDao().upsert(local);
            db.sectionDao().delete(localId, ownerId);
        }
        db.studentDao().reassignSection(localId, serverId, ownerId);
        db.attendanceSessionDao().reassignSection(localId, serverId, ownerId);
        db.attendanceRecordDao().reassignSection(localId, serverId, ownerId);
    }

    public void remapSubjectId(String localId, String serverId) {
        if (skipRemap(localId, serverId)) {
            return;
        }
        String ownerId = ownerId();
        SubjectEntity local = db.subjectDao().findOwned(localId, ownerId);
        if (local != null) {
            local.id = serverId;
            db.subjectDao().upsert(local);
            db.subjectDao().delete(localId, ownerId);
        }
        db.attendanceSessionDao().reassignSubject(localId, serverId, ownerId);
    }

    public void remapStudentId(String localId, String serverId) {
        if (skipRemap(localId, serverId)) {
            return;
        }
        String ownerId = ownerId();
        StudentEntity local = db.studentDao().findOwned(localId, ownerId);
        if (local != null) {
            db.studentDao().delete(localId, ownerId);
            local.id = serverId;
            db.studentDao().upsert(local);
        }
        db.attendanceRecordDao().reassignStudent(localId, serverId, ownerId);
        db.adjustmentDao().reassignStudent(localId, serverId, ownerId);
        db.notificationDao().reassignStudent(localId, serverId, ownerId);
    }

    private static boolean skipRemap(String localId, String serverId) {
        return localId == null || serverId == null || localId.equals(serverId);
    }
}
