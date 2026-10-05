package com.example.studentattendance.data.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.LectureEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;

/**
 * Deletes one class/course card and every record that belongs only to that
 * exact subject ID. Other departments/sections/courses are left untouched.
 */
public final class ClassSetupCascadeDelete {

    private ClassSetupCascadeDelete() {
    }

    public static void execute(AppDatabase db, String ownerId, String subjectId) {
        db.runInTransaction((Callable<Void>) () -> {
            delete(new RoomGraph(db), ownerId, subjectId);
            return null;
        });
    }

    static void delete(Graph graph, String ownerId, String subjectId) {
        if (graph == null) {
            throw new LocalDataException("Class setup could not be deleted.");
        }
        SubjectEntity subject = graph.requireSubject(ownerId, subjectId);
        String sectionId = subject.sectionId;
        String semesterId = subject.semesterId;
        String departmentId = subject.departmentId;

        for (AttendanceSessionEntity session : graph.sessionsForSubject(ownerId, subjectId)) {
            graph.deleteSessionAndRecords(ownerId, session.id);
        }
        for (LectureEntity lecture : graph.lecturesForCourse(ownerId, sectionId, subjectId)) {
            graph.deleteLecture(ownerId, lecture.id);
        }

        boolean lastCourseInSection = graph.subjectCountInSection(ownerId, sectionId) <= 1;
        if (lastCourseInSection) {
            for (StudentEntity student : graph.studentsInSection(ownerId, sectionId)) {
                graph.deleteStudentDependents(ownerId, student.id);
            }
        }

        graph.deleteSubject(ownerId, subjectId);

        if (lastCourseInSection && notBlank(sectionId)) {
            graph.deleteSection(ownerId, sectionId);
        }
        if (notBlank(semesterId) && graph.sectionCountInSemester(ownerId, semesterId) == 0
                && graph.subjectCountInSemester(ownerId, semesterId) == 0) {
            graph.deleteSemester(ownerId, semesterId);
        }
        if (notBlank(departmentId) && graph.semesterCountInDepartment(ownerId, departmentId) == 0
                && graph.subjectCountInDepartment(ownerId, departmentId) == 0) {
            graph.deleteDepartment(ownerId, departmentId);
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isEmpty();
    }

    interface Graph {
        SubjectEntity requireSubject(String ownerId, String subjectId);

        List<AttendanceSessionEntity> sessionsForSubject(String ownerId, String subjectId);

        void deleteSessionAndRecords(String ownerId, String sessionId);

        List<LectureEntity> lecturesForCourse(String ownerId, String sectionId, String subjectId);

        void deleteLecture(String ownerId, String lectureId);

        int subjectCountInSection(String ownerId, String sectionId);

        List<StudentEntity> studentsInSection(String ownerId, String sectionId);

        void deleteStudentDependents(String ownerId, String studentId);

        void deleteSubject(String ownerId, String subjectId);

        void deleteSection(String ownerId, String sectionId);

        void deleteSemester(String ownerId, String semesterId);

        void deleteDepartment(String ownerId, String departmentId);

        int sectionCountInSemester(String ownerId, String semesterId);

        int subjectCountInSemester(String ownerId, String semesterId);

        int semesterCountInDepartment(String ownerId, String departmentId);

        int subjectCountInDepartment(String ownerId, String departmentId);
    }

    private static final class RoomGraph implements Graph {
        private final AppDatabase db;

        RoomGraph(AppDatabase db) {
            this.db = db;
        }

        @Override
        public SubjectEntity requireSubject(String ownerId, String subjectId) {
            SubjectEntity entity = db.subjectDao().findOwned(subjectId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Subject not found.");
            }
            return entity;
        }

        @Override
        public List<AttendanceSessionEntity> sessionsForSubject(String ownerId, String subjectId) {
            return db.attendanceSessionDao().listBySubject(ownerId, subjectId);
        }

        @Override
        public void deleteSessionAndRecords(String ownerId, String sessionId) {
            db.attendanceRecordDao().deleteBySession(sessionId, ownerId);
            db.attendanceSessionDao().delete(sessionId, ownerId);
        }

        @Override
        public List<LectureEntity> lecturesForCourse(String ownerId, String sectionId, String subjectId) {
            if (sectionId == null || subjectId == null) {
                return new ArrayList<>();
            }
            return db.lectureDao().listBySectionAndSubject(ownerId, sectionId, subjectId);
        }

        @Override
        public void deleteLecture(String ownerId, String lectureId) {
            db.lectureDao().delete(lectureId, ownerId);
        }

        @Override
        public int subjectCountInSection(String ownerId, String sectionId) {
            if (sectionId == null || sectionId.isEmpty()) {
                return 0;
            }
            return db.subjectDao().countInSection(ownerId, sectionId);
        }

        @Override
        public List<StudentEntity> studentsInSection(String ownerId, String sectionId) {
            if (sectionId == null || sectionId.isEmpty()) {
                return new ArrayList<>();
            }
            return db.studentDao().listAllInSection(ownerId, sectionId);
        }

        @Override
        public void deleteStudentDependents(String ownerId, String studentId) {
            db.notificationDao().deleteByRelatedStudent(ownerId, studentId);
            db.studentDao().delete(studentId, ownerId);
        }

        @Override
        public void deleteSubject(String ownerId, String subjectId) {
            db.subjectDao().delete(subjectId, ownerId);
        }

        @Override
        public void deleteSection(String ownerId, String sectionId) {
            db.sectionDao().delete(sectionId, ownerId);
        }

        @Override
        public void deleteSemester(String ownerId, String semesterId) {
            db.semesterDao().delete(semesterId, ownerId);
        }

        @Override
        public void deleteDepartment(String ownerId, String departmentId) {
            db.departmentDao().delete(departmentId, ownerId);
        }

        @Override
        public int sectionCountInSemester(String ownerId, String semesterId) {
            return db.sectionDao().listBySemester(ownerId, semesterId).size();
        }

        @Override
        public int subjectCountInSemester(String ownerId, String semesterId) {
            return db.subjectDao().countInSemester(ownerId, semesterId);
        }

        @Override
        public int semesterCountInDepartment(String ownerId, String departmentId) {
            return db.semesterDao().listByDepartment(ownerId, departmentId).size();
        }

        @Override
        public int subjectCountInDepartment(String ownerId, String departmentId) {
            return db.subjectDao().countInDepartment(ownerId, departmentId);
        }
    }

    /** In-memory graph used by unit tests. Not a second database. */
    static final class MemoryGraph implements Graph {
        final List<SubjectEntity> subjects = new ArrayList<>();
        final List<AttendanceSessionEntity> sessions = new ArrayList<>();
        final List<String> deletedSessionIds = new ArrayList<>();
        final List<LectureEntity> lectures = new ArrayList<>();
        final List<StudentEntity> students = new ArrayList<>();
        final List<String> deletedStudentIds = new ArrayList<>();
        final List<String> deletedLectureIds = new ArrayList<>();
        final java.util.Map<String, String> sectionToSemester = new java.util.LinkedHashMap<>();
        final java.util.Map<String, String> semesterToDepartment = new java.util.LinkedHashMap<>();
        final java.util.Set<String> departments = new java.util.LinkedHashSet<>();
        final List<String> deletedNotifications = new ArrayList<>();

        @Override
        public SubjectEntity requireSubject(String ownerId, String subjectId) {
            for (SubjectEntity subject : subjects) {
                if (subjectId.equals(subject.id) && ownerId.equals(subject.ownerUserId)) {
                    return subject;
                }
            }
            throw new LocalDataException("Subject not found.");
        }

        @Override
        public List<AttendanceSessionEntity> sessionsForSubject(String ownerId, String subjectId) {
            List<AttendanceSessionEntity> result = new ArrayList<>();
            for (AttendanceSessionEntity session : sessions) {
                if (ownerId.equals(session.ownerUserId) && subjectId.equals(session.subjectId)) {
                    result.add(session);
                }
            }
            return result;
        }

        @Override
        public void deleteSessionAndRecords(String ownerId, String sessionId) {
            deletedSessionIds.add(sessionId);
            Iterator<AttendanceSessionEntity> iterator = sessions.iterator();
            while (iterator.hasNext()) {
                AttendanceSessionEntity session = iterator.next();
                if (sessionId.equals(session.id) && ownerId.equals(session.ownerUserId)) {
                    iterator.remove();
                }
            }
        }

        @Override
        public List<LectureEntity> lecturesForCourse(String ownerId, String sectionId, String subjectId) {
            List<LectureEntity> result = new ArrayList<>();
            for (LectureEntity lecture : lectures) {
                if (ownerId.equals(lecture.ownerUserId)
                        && equalsNullable(sectionId, lecture.sectionId)
                        && equalsNullable(subjectId, lecture.subjectId)) {
                    result.add(lecture);
                }
            }
            return result;
        }

        @Override
        public void deleteLecture(String ownerId, String lectureId) {
            deletedLectureIds.add(lectureId);
            lectures.removeIf(lecture -> lectureId.equals(lecture.id) && ownerId.equals(lecture.ownerUserId));
        }

        @Override
        public int subjectCountInSection(String ownerId, String sectionId) {
            int count = 0;
            for (SubjectEntity subject : subjects) {
                if (ownerId.equals(subject.ownerUserId) && equalsNullable(sectionId, subject.sectionId)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public List<StudentEntity> studentsInSection(String ownerId, String sectionId) {
            List<StudentEntity> result = new ArrayList<>();
            for (StudentEntity student : students) {
                if (ownerId.equals(student.ownerUserId) && equalsNullable(sectionId, student.sectionId)) {
                    result.add(student);
                }
            }
            return result;
        }

        @Override
        public void deleteStudentDependents(String ownerId, String studentId) {
            deletedNotifications.add(studentId);
            deletedStudentIds.add(studentId);
            students.removeIf(student -> studentId.equals(student.id) && ownerId.equals(student.ownerUserId));
        }

        @Override
        public void deleteSubject(String ownerId, String subjectId) {
            subjects.removeIf(subject -> subjectId.equals(subject.id) && ownerId.equals(subject.ownerUserId));
        }

        @Override
        public void deleteSection(String ownerId, String sectionId) {
            sectionToSemester.remove(sectionId);
        }

        @Override
        public void deleteSemester(String ownerId, String semesterId) {
            semesterToDepartment.remove(semesterId);
        }

        @Override
        public void deleteDepartment(String ownerId, String departmentId) {
            departments.remove(departmentId);
        }

        @Override
        public int sectionCountInSemester(String ownerId, String semesterId) {
            int count = 0;
            for (String parent : sectionToSemester.values()) {
                if (semesterId.equals(parent)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int subjectCountInSemester(String ownerId, String semesterId) {
            int count = 0;
            for (SubjectEntity subject : subjects) {
                if (ownerId.equals(subject.ownerUserId) && equalsNullable(semesterId, subject.semesterId)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int semesterCountInDepartment(String ownerId, String departmentId) {
            int count = 0;
            for (String parent : semesterToDepartment.values()) {
                if (departmentId.equals(parent)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int subjectCountInDepartment(String ownerId, String departmentId) {
            int count = 0;
            for (SubjectEntity subject : subjects) {
                if (ownerId.equals(subject.ownerUserId) && equalsNullable(departmentId, subject.departmentId)) {
                    count++;
                }
            }
            return count;
        }

        private static boolean equalsNullable(String left, String right) {
            return left == null ? right == null : left.equals(right);
        }
    }
}
