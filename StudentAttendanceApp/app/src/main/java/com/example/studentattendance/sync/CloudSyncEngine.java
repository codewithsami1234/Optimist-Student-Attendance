package com.example.studentattendance.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiErrorParser;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.network.dto.AttendanceAdjustmentRequestDto;
import com.example.studentattendance.network.dto.AttendanceRecordInputDto;
import com.example.studentattendance.network.dto.AttendanceSaveRequestDto;
import com.example.studentattendance.network.dto.DepartmentCreateRequestDto;
import com.example.studentattendance.network.dto.DepartmentUpdateRequestDto;
import com.example.studentattendance.network.dto.SectionCreateRequestDto;
import com.example.studentattendance.network.dto.SectionUpdateRequestDto;
import com.example.studentattendance.network.dto.SemesterCreateRequestDto;
import com.example.studentattendance.network.dto.SemesterUpdateRequestDto;
import com.example.studentattendance.network.dto.StudentTransferRequestDto;
import com.example.studentattendance.network.dto.StudentUpdateRequestDto;
import com.example.studentattendance.network.dto.SubjectCreateRequestDto;
import com.example.studentattendance.network.dto.SubjectUpdateRequestDto;
import com.example.studentattendance.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Replays the outbox against the live API and refreshes the notification inbox.
 * Runs on a WorkManager background thread using synchronous Retrofit calls.
 */
public final class CloudSyncEngine {

    public enum Outcome { SUCCESS, RETRY, NOTHING }

    private final Context appContext;
    private final PendingSyncStore store;
    private final RoomCacheStore cache;
    private final AppDatabase db;
    private final PreferenceManager prefs;

    public CloudSyncEngine(Context context, PendingSyncStore store, RoomCacheStore cache, AppDatabase db) {
        this.appContext = context.getApplicationContext();
        this.store = store;
        this.cache = cache;
        this.db = db;
        this.prefs = new PreferenceManager(appContext);
    }

    public Outcome sync() {
        if (prefs.getAuthToken() == null || prefs.getSavedUser() == null) {
            return Outcome.NOTHING;
        }
        boolean needRetry = false;
        List<PendingOp> ops = store.snapshot();
        List<PendingOp> remaining = new ArrayList<>();
        for (int i = 0; i < ops.size(); i++) {
            PendingOp op = ops.get(i);
            try {
                replay(op);
            } catch (RetryException e) {
                op.attempts++;
                remaining.addAll(ops.subList(i, ops.size()));
                needRetry = true;
                break;
            } catch (FailException e) {
                op.attempts++;
                if (op.attempts < 8 && shouldKeep(e.getMessage())) {
                    remaining.addAll(ops.subList(i, ops.size()));
                    needRetry = true;
                    break;
                }
            }
        }
        store.replaceAll(remaining);
        refreshNotifications();
        return needRetry ? Outcome.RETRY : Outcome.SUCCESS;
    }

    private void replay(PendingOp op) throws RetryException, FailException {
        Map<String, Object> p = op.payload == null ? new LinkedHashMap<>() : op.payload;
        String type = op.type;
        if (SyncOpType.CREATE_DEPARTMENT.equals(type)) {
            Department created = execute(api().createDepartment(new DepartmentCreateRequestDto(
                    PendingSyncStore.str(p, "name"),
                    PendingSyncStore.str(p, "code"),
                    PendingSyncStore.str(p, "headOfDepartment"))));
            requireBody(created);
            finishCreate(op.localId, created.getId(), () -> {
                cache.remapDepartmentId(op.localId, created.getId());
                cache.upsertDepartment(created);
            });
        } else if (SyncOpType.UPDATE_DEPARTMENT.equals(type)) {
            Department updated = execute(api().updateDepartment(store.resolveId(PendingSyncStore.str(p, "id")),
                    new DepartmentUpdateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.str(p, "code"),
                            PendingSyncStore.str(p, "headOfDepartment"),
                            PendingSyncStore.bool(p, "archived"))));
            cache.upsertDepartment(updated);
        } else if (SyncOpType.DELETE_DEPARTMENT.equals(type)) {
            execute(api().deleteDepartment(store.resolveId(PendingSyncStore.str(p, "id"))));
        } else if (SyncOpType.CREATE_SEMESTER.equals(type)) {
            Semester created = execute(api().createSemester(
                    store.resolveId(PendingSyncStore.str(p, "departmentId")),
                    new SemesterCreateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.integer(p, "semesterNumber") == null
                                    ? 1 : PendingSyncStore.integer(p, "semesterNumber"))));
            requireBody(created);
            finishCreate(op.localId, created.getId(), () -> {
                cache.remapSemesterId(op.localId, created.getId());
                cache.upsertSemester(created);
            });
        } else if (SyncOpType.UPDATE_SEMESTER.equals(type)) {
            Semester updated = execute(api().updateSemester(store.resolveId(PendingSyncStore.str(p, "id")),
                    new SemesterUpdateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.integer(p, "semesterNumber"),
                            PendingSyncStore.bool(p, "archived"))));
            cache.upsertSemester(updated);
        } else if (SyncOpType.DELETE_SEMESTER.equals(type)) {
            execute(api().deleteSemester(store.resolveId(PendingSyncStore.str(p, "id"))));
        } else if (SyncOpType.CREATE_SECTION.equals(type)) {
            Section created = execute(api().createSection(
                    store.resolveId(PendingSyncStore.str(p, "semesterId")),
                    new SectionCreateRequestDto(PendingSyncStore.str(p, "name"))));
            requireBody(created);
            finishCreate(op.localId, created.getId(), () -> {
                cache.remapSectionId(op.localId, created.getId());
                cache.upsertSection(created);
            });
        } else if (SyncOpType.UPDATE_SECTION.equals(type)) {
            Section updated = execute(api().updateSection(store.resolveId(PendingSyncStore.str(p, "id")),
                    new SectionUpdateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.bool(p, "archived"))));
            cache.upsertSection(updated);
        } else if (SyncOpType.DELETE_SECTION.equals(type)) {
            execute(api().deleteSection(store.resolveId(PendingSyncStore.str(p, "id"))));
        } else if (SyncOpType.CREATE_SUBJECT.equals(type)) {
            Subject created = execute(api().createSubject(
                    store.resolveId(PendingSyncStore.str(p, "departmentId")),
                    new SubjectCreateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.str(p, "code"))));
            requireBody(created);
            finishCreate(op.localId, created.getId(), () -> {
                cache.remapSubjectId(op.localId, created.getId());
                cache.upsertSubject(created);
            });
        } else if (SyncOpType.UPDATE_SUBJECT.equals(type)) {
            Subject updated = execute(api().updateSubject(store.resolveId(PendingSyncStore.str(p, "id")),
                    new SubjectUpdateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.str(p, "code"),
                            PendingSyncStore.bool(p, "archived"))));
            cache.upsertSubject(updated);
        } else if (SyncOpType.DELETE_SUBJECT.equals(type)) {
            execute(api().deleteSubject(store.resolveId(PendingSyncStore.str(p, "id"))));
        } else if (SyncOpType.ADD_STUDENT.equals(type)) {
            Student body = studentFrom(p);
            body.setDepartmentId(store.resolveId(body.getDepartmentId()));
            body.setSemesterId(store.resolveId(body.getSemesterId()));
            body.setSectionId(store.resolveId(body.getSectionId()));
            Student created = execute(api().addStudent(body));
            requireBody(created);
            finishCreate(op.localId, created.getId(), () -> {
                cache.remapStudentId(op.localId, created.getId());
                cache.upsertStudent(created);
            });
        } else if (SyncOpType.UPDATE_STUDENT.equals(type)) {
            Student updated = execute(api().updateStudent(store.resolveId(PendingSyncStore.str(p, "id")),
                    new StudentUpdateRequestDto(
                            PendingSyncStore.str(p, "name"),
                            PendingSyncStore.str(p, "rollNumber"),
                            PendingSyncStore.str(p, "email"),
                            PendingSyncStore.str(p, "phone"),
                            PendingSyncStore.bool(p, "archived"))));
            cache.upsertStudent(updated);
        } else if (SyncOpType.TRANSFER_STUDENT.equals(type)) {
            Student updated = execute(api().transferStudent(
                    store.resolveId(PendingSyncStore.str(p, "id")),
                    new StudentTransferRequestDto(store.resolveId(PendingSyncStore.str(p, "sectionId")))));
            cache.upsertStudent(updated);
        } else if (SyncOpType.DELETE_STUDENT.equals(type)) {
            execute(api().deleteStudent(store.resolveId(PendingSyncStore.str(p, "id"))));
        } else if (SyncOpType.IMPORT_STUDENTS.equals(type)) {
            List<Student> students = studentsFrom(p.get("students"));
            for (Student student : students) {
                student.setDepartmentId(store.resolveId(student.getDepartmentId()));
                student.setSemesterId(store.resolveId(student.getSemesterId()));
                student.setSectionId(store.resolveId(student.getSectionId()));
            }
            ImportResult result = execute(api().importStudents(students));
            if (result != null) {
                store.markPullSuccess();
            }
        } else if (SyncOpType.SAVE_ATTENDANCE.equals(type)) {
            AttendanceSaveRequestDto request = attendanceRequest(p);
            AttendanceSession saved = execute(api().saveAttendanceSession(request));
            requireBody(saved);
            cache.upsertSession(saved);
        } else if (SyncOpType.CREATE_ADJUSTMENT.equals(type)) {
            Integer delta = PendingSyncStore.integer(p, "delta");
            AttendanceAdjustment created = execute(api().createAdjustment(
                    store.resolveId(PendingSyncStore.str(p, "studentId")),
                    new AttendanceAdjustmentRequestDto(delta == null ? 0 : delta, PendingSyncStore.str(p, "reason"))));
            requireBody(created);
            cache.upsertAdjustment(created);
        } else if (SyncOpType.MARK_NOTIFICATION_READ.equals(type)) {
            execute(api().markNotificationRead(PendingSyncStore.str(p, "id")));
        } else if (SyncOpType.MARK_ALL_NOTIFICATIONS_READ.equals(type)) {
            execute(api().markAllNotificationsRead());
        }
    }

    public void refreshNotifications() {
        try {
            List<Notification> notifications = execute(api().getNotifications(0, 50));
            if (notifications != null) {
                cache.upsertNotifications(notifications);
                store.markPullSuccess();
                AppNotificationHelper.publishUnread(appContext, notifications, store);
            }
        } catch (Exception ignored) {
            List<Notification> cached = new ArrayList<>();
            try {
                String ownerId = cache.ownerId();
                for (NotificationEntity entity : db.notificationDao().list(ownerId)) {
                    cached.add(entity.toModel());
                }
                AppNotificationHelper.publishUnread(appContext, cached, store);
            } catch (Exception ignoredToo) {
                // No session / empty cache.
            }
        }
    }

    private void finishCreate(String localId, String serverId, Runnable remap) {
        if (localId != null && serverId != null) {
            store.mapId(localId, serverId);
            remap.run();
        }
    }

    private AttendanceSaveRequestDto attendanceRequest(Map<String, Object> p) {
        @SuppressWarnings("unchecked")
        Map<String, Object> statusMap = (Map<String, Object>) p.get("statusByStudentId");
        @SuppressWarnings("unchecked")
        Map<String, Object> names = (Map<String, Object>) p.get("nameByStudentId");
        @SuppressWarnings("unchecked")
        Map<String, Object> rolls = (Map<String, Object>) p.get("rollByStudentId");
        List<AttendanceRecordInputDto> records = new ArrayList<>();
        if (statusMap != null) {
            for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
                String studentId = store.resolveId(entry.getKey());
                String name = names == null || names.get(entry.getKey()) == null
                        ? "" : String.valueOf(names.get(entry.getKey()));
                String roll = rolls == null || rolls.get(entry.getKey()) == null
                        ? "" : String.valueOf(rolls.get(entry.getKey()));
                AttendanceStatus status = AttendanceStatus.valueOf(String.valueOf(entry.getValue()));
                records.add(new AttendanceRecordInputDto(studentId, name, roll, status));
            }
        }
        return new AttendanceSaveRequestDto(
                store.resolveId(PendingSyncStore.str(p, "departmentId")),
                PendingSyncStore.str(p, "departmentName"),
                store.resolveId(PendingSyncStore.str(p, "semesterId")),
                PendingSyncStore.str(p, "semesterName"),
                store.resolveId(PendingSyncStore.str(p, "sectionId")),
                PendingSyncStore.str(p, "sectionName"),
                store.resolveId(PendingSyncStore.str(p, "subjectId")),
                PendingSyncStore.str(p, "subjectName"),
                PendingSyncStore.str(p, "date"),
                records);
    }

    private Student studentFrom(Map<String, Object> p) {
        Student student = new Student(
                PendingSyncStore.str(p, "id"),
                PendingSyncStore.str(p, "rollNumber"),
                PendingSyncStore.str(p, "name"),
                PendingSyncStore.str(p, "departmentId"),
                PendingSyncStore.str(p, "semesterId"),
                PendingSyncStore.str(p, "sectionId"),
                PendingSyncStore.str(p, "email"),
                PendingSyncStore.str(p, "phone"));
        Boolean archived = PendingSyncStore.bool(p, "archived");
        if (archived != null) {
            student.setArchived(archived);
        }
        return student;
    }

    @SuppressWarnings("unchecked")
    private List<Student> studentsFrom(Object raw) {
        List<Student> result = new ArrayList<>();
        if (!(raw instanceof List)) {
            return result;
        }
        for (Object row : (List<?>) raw) {
            if (row instanceof Map) {
                result.add(studentFrom((Map<String, Object>) row));
            }
        }
        return result;
    }

    private static <T> T requireBody(T body) throws FailException {
        if (body == null) {
            throw new FailException("The server returned an empty response.");
        }
        return body;
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    private <T> T execute(Call<T> call) throws RetryException, FailException {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            String message = ApiErrorParser.parse(response);
            if (RetrofitExec.isRetryableHttp(response.code())) {
                throw new RetryException(message);
            }
            throw new FailException(message);
        } catch (IOException e) {
            throw new RetryException(ApiErrorParser.networkFailureMessage(e));
        }
    }

    private static boolean shouldKeep(String message) {
        if (message == null) {
            return true;
        }
        String lower = message.toLowerCase();
        return lower.contains("not found")
                || lower.contains("don't have access")
                || lower.contains("could not")
                || lower.contains("connect");
    }

    static final class RetryException extends Exception {
        RetryException(String message) {
            super(message);
        }
    }

    static final class FailException extends Exception {
        FailException(String message) {
            super(message);
        }
    }
}
