package com.example.studentattendance.data.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Permanently removes academic/attendance data for one teacher while leaving
 * the authentication account in Room.
 */
public final class AcademicDataReset {

    private AcademicDataReset() {
    }

    public static void execute(AppDatabase db, String ownerId) {
        if (db == null || ownerId == null || ownerId.isEmpty()) {
            throw new LocalDataException("Could not reset academic data.");
        }
        db.runInTransaction((Callable<Void>) () -> {
            delete(new RoomGraph(db), ownerId);
            return null;
        });
    }

    static void delete(Graph graph, String ownerId) {
        if (graph == null || ownerId == null || ownerId.isEmpty()) {
            throw new LocalDataException("Could not reset academic data.");
        }
        graph.deleteNotifications(ownerId);
        graph.deleteAttendanceRecords(ownerId);
        graph.deleteAttendanceSessions(ownerId);
        graph.deleteLectures(ownerId);
        graph.deleteAdjustments(ownerId);
        graph.deleteStudents(ownerId);
        graph.deleteSubjects(ownerId);
        graph.deleteSections(ownerId);
        graph.deleteSemesters(ownerId);
        graph.deleteDepartments(ownerId);
    }

    interface Graph {
        void deleteNotifications(String ownerId);

        void deleteAttendanceRecords(String ownerId);

        void deleteAttendanceSessions(String ownerId);

        void deleteLectures(String ownerId);

        void deleteAdjustments(String ownerId);

        void deleteStudents(String ownerId);

        void deleteSubjects(String ownerId);

        void deleteSections(String ownerId);

        void deleteSemesters(String ownerId);

        void deleteDepartments(String ownerId);
    }

    private static final class RoomGraph implements Graph {
        private final AppDatabase db;

        RoomGraph(AppDatabase db) {
            this.db = db;
        }

        @Override
        public void deleteNotifications(String ownerId) {
            db.notificationDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteAttendanceRecords(String ownerId) {
            db.attendanceRecordDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteAttendanceSessions(String ownerId) {
            db.attendanceSessionDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteLectures(String ownerId) {
            db.lectureDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteAdjustments(String ownerId) {
            db.adjustmentDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteStudents(String ownerId) {
            db.studentDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteSubjects(String ownerId) {
            db.subjectDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteSections(String ownerId) {
            db.sectionDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteSemesters(String ownerId) {
            db.semesterDao().deleteByOwner(ownerId);
        }

        @Override
        public void deleteDepartments(String ownerId) {
            db.departmentDao().deleteByOwner(ownerId);
        }
    }

    /** In-memory graph used by unit tests. Accounts are never cleared. */
    public static final class MemoryGraph implements Graph {
        public final List<String> notifications = new ArrayList<>();
        public final List<String> records = new ArrayList<>();
        public final List<String> sessions = new ArrayList<>();
        public final List<String> lectures = new ArrayList<>();
        public final List<String> adjustments = new ArrayList<>();
        public final List<String> students = new ArrayList<>();
        public final List<String> subjects = new ArrayList<>();
        public final List<String> sections = new ArrayList<>();
        public final List<String> semesters = new ArrayList<>();
        public final List<String> departments = new ArrayList<>();
        public final List<String> accounts = new ArrayList<>();

        public void seedOwner(String ownerId) {
            notifications.add(ownerId + ":n1");
            records.add(ownerId + ":r1");
            sessions.add(ownerId + ":s1");
            lectures.add(ownerId + ":l1");
            adjustments.add(ownerId + ":a1");
            students.add(ownerId + ":st1");
            subjects.add(ownerId + ":sub1");
            sections.add(ownerId + ":sec1");
            semesters.add(ownerId + ":sem1");
            departments.add(ownerId + ":d1");
            accounts.add(ownerId + ":account");
        }

        @Override
        public void deleteNotifications(String ownerId) {
            removeOwned(notifications, ownerId);
        }

        @Override
        public void deleteAttendanceRecords(String ownerId) {
            removeOwned(records, ownerId);
        }

        @Override
        public void deleteAttendanceSessions(String ownerId) {
            removeOwned(sessions, ownerId);
        }

        @Override
        public void deleteLectures(String ownerId) {
            removeOwned(lectures, ownerId);
        }

        @Override
        public void deleteAdjustments(String ownerId) {
            removeOwned(adjustments, ownerId);
        }

        @Override
        public void deleteStudents(String ownerId) {
            removeOwned(students, ownerId);
        }

        @Override
        public void deleteSubjects(String ownerId) {
            removeOwned(subjects, ownerId);
        }

        @Override
        public void deleteSections(String ownerId) {
            removeOwned(sections, ownerId);
        }

        @Override
        public void deleteSemesters(String ownerId) {
            removeOwned(semesters, ownerId);
        }

        @Override
        public void deleteDepartments(String ownerId) {
            removeOwned(departments, ownerId);
        }

        public boolean hasOrphans(String ownerId) {
            return containsOwner(notifications, ownerId)
                    || containsOwner(records, ownerId)
                    || containsOwner(sessions, ownerId)
                    || containsOwner(lectures, ownerId)
                    || containsOwner(adjustments, ownerId)
                    || containsOwner(students, ownerId)
                    || containsOwner(subjects, ownerId)
                    || containsOwner(sections, ownerId)
                    || containsOwner(semesters, ownerId)
                    || containsOwner(departments, ownerId);
        }

        private static void removeOwned(List<String> values, String ownerId) {
            Iterator<String> iterator = values.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().startsWith(ownerId + ":")) {
                    iterator.remove();
                }
            }
        }

        private static boolean containsOwner(List<String> values, String ownerId) {
            for (String value : values) {
                if (value != null && value.startsWith(ownerId + ":")) {
                    return true;
                }
            }
            return false;
        }
    }
}
