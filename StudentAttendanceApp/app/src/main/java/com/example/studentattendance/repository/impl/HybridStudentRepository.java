package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.HybridWrite;
import com.example.studentattendance.sync.PendingOp;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.RoomCacheStore;
import com.example.studentattendance.sync.SyncOpType;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridStudentRepository implements StudentRepository {

    private final Context appContext;
    private final RetrofitStudentRepository remote;
    private final RoomStudentRepository local;
    private final RoomCacheStore cache;
    private final PendingSyncStore store;

    public HybridStudentRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                    RoomCacheStore cache, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitStudentRepository(appContext);
        this.local = new RoomStudentRepository(prefs, db);
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void getStudents(String departmentId, String semesterId, String sectionId,
                             ApiCallback<List<Student>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getStudents(store.resolveId(departmentId), store.resolveId(semesterId),
                        store.resolveId(sectionId), cb),
                cb -> local.getStudents(departmentId, semesterId, sectionId, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Student student : list) {
                        cache.upsertStudent(student);
                    }
                },
                false, callback);
    }

    @Override
    public void getAllStudents(ApiCallback<List<Student>> callback) {
        HybridRead.fetch(appContext, store, remote::getAllStudents, local::getAllStudents,
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Student student : list) {
                        cache.upsertStudent(student);
                    }
                },
                false, callback);
    }

    @Override
    public void addStudent(Student student, ApiCallback<Student> callback) {
        Student remoteStudent = copyWithResolvedIds(student);
        HybridWrite.mutate(appContext,
                cb -> remote.addStudent(remoteStudent, cb),
                cb -> local.addStudent(student, cb),
                cache::upsertStudent,
                created -> store.enqueue(PendingOp.create(SyncOpType.ADD_STUDENT, created.getId(), studentPayload(created))),
                callback);
    }

    @Override
    public void updateStudent(String studentId, String name, String rollNumber, String email, String phone,
                               Boolean archived, ApiCallback<Student> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.updateStudent(store.resolveId(studentId), name, rollNumber, email, phone, archived, cb),
                cb -> local.updateStudent(studentId, name, rollNumber, email, phone, archived, cb),
                cache::upsertStudent,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", studentId);
                    payload.put("name", name);
                    payload.put("rollNumber", rollNumber);
                    payload.put("email", email);
                    payload.put("phone", phone);
                    payload.put("archived", archived);
                    store.enqueue(PendingOp.create(SyncOpType.UPDATE_STUDENT, null, payload));
                },
                callback);
    }

    @Override
    public void transferStudent(String studentId, String sectionId, ApiCallback<Student> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.transferStudent(store.resolveId(studentId), store.resolveId(sectionId), cb),
                cb -> local.transferStudent(studentId, sectionId, cb),
                cache::upsertStudent,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", studentId);
                    payload.put("sectionId", sectionId);
                    store.enqueue(PendingOp.create(SyncOpType.TRANSFER_STUDENT, null, payload));
                },
                callback);
    }

    @Override
    public void deleteStudent(String studentId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.deleteStudent(store.resolveId(studentId), cb),
                cb -> local.deleteStudent(studentId, cb),
                () -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", studentId);
                    store.enqueue(PendingOp.create(SyncOpType.DELETE_STUDENT, null, payload));
                },
                callback);
    }

    @Override
    public void importStudents(List<Student> students, ApiCallback<ImportResult> callback) {
        List<Student> remoteStudents = new ArrayList<>();
        if (students != null) {
            for (Student student : students) {
                remoteStudents.add(copyWithResolvedIds(student));
            }
        }
        HybridWrite.mutate(appContext,
                cb -> remote.importStudents(remoteStudents, cb),
                cb -> local.importStudents(students, cb),
                ignored -> { },
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    List<Map<String, Object>> rows = new ArrayList<>();
                    if (students != null) {
                        for (Student student : students) {
                            rows.add(studentPayload(student));
                        }
                    }
                    payload.put("students", rows);
                    store.enqueue(PendingOp.create(SyncOpType.IMPORT_STUDENTS, null, payload));
                },
                callback);
    }

    private Student copyWithResolvedIds(Student student) {
        if (student == null) {
            return null;
        }
        Student copy = new Student(student.getId(), student.getRollNumber(), student.getName(),
                store.resolveId(student.getDepartmentId()),
                store.resolveId(student.getSemesterId()),
                store.resolveId(student.getSectionId()),
                student.getEmail(), student.getPhone());
        copy.setArchived(student.isArchived());
        return copy;
    }

    private Map<String, Object> studentPayload(Student student) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (student == null) {
            return payload;
        }
        payload.put("id", student.getId());
        payload.put("rollNumber", student.getRollNumber());
        payload.put("name", student.getName());
        payload.put("departmentId", student.getDepartmentId());
        payload.put("semesterId", student.getSemesterId());
        payload.put("sectionId", student.getSectionId());
        payload.put("email", student.getEmail());
        payload.put("phone", student.getPhone());
        payload.put("archived", student.isArchived());
        return payload;
    }
}
