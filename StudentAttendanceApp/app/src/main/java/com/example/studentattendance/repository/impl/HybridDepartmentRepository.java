package com.example.studentattendance.repository.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Lecture;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.HybridWrite;
import com.example.studentattendance.sync.PendingOp;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.RoomCacheStore;
import com.example.studentattendance.sync.SyncOpType;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridDepartmentRepository implements DepartmentRepository {

    private final Context appContext;
    private final RetrofitDepartmentRepository remote;
    private final RoomDepartmentRepository local;
    private final RoomCacheStore cache;
    private final PendingSyncStore store;

    public HybridDepartmentRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                      RoomCacheStore cache, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitDepartmentRepository(appContext);
        this.local = new RoomDepartmentRepository(prefs, db);
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void getDepartments(ApiCallback<List<Department>> callback) {
        HybridRead.fetch(appContext, store,
                remote::getDepartments,
                local::getDepartments,
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Department department : list) {
                        cache.upsertDepartment(department);
                    }
                },
                false, callback);
    }

    @Override
    public void getClassSetups(ApiCallback<List<ClassSetup>> callback) {
        local.getClassSetups(callback);
    }

    @Override
    public void getSemesters(String departmentId, ApiCallback<List<Semester>> callback) {
        String resolved = store.resolveId(departmentId);
        HybridRead.fetch(appContext, store,
                cb -> remote.getSemesters(resolved, cb),
                cb -> local.getSemesters(departmentId, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Semester semester : list) {
                        cache.upsertSemester(semester);
                    }
                },
                false, callback);
    }

    @Override
    public void getSections(String semesterId, ApiCallback<List<Section>> callback) {
        String resolved = store.resolveId(semesterId);
        HybridRead.fetch(appContext, store,
                cb -> remote.getSections(resolved, cb),
                cb -> local.getSections(semesterId, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Section section : list) {
                        cache.upsertSection(section);
                    }
                },
                false, callback);
    }

    @Override
    public void getSubjects(String departmentId, ApiCallback<List<Subject>> callback) {
        String resolved = store.resolveId(departmentId);
        HybridRead.fetch(appContext, store,
                cb -> remote.getSubjects(resolved, cb),
                cb -> local.getSubjects(departmentId, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (Subject subject : list) {
                        cache.upsertSubject(subject);
                    }
                },
                false, callback);
    }

    @Override
    public void createDepartment(String name, String code, String headOfDepartment, ApiCallback<Department> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.createDepartment(name, code, headOfDepartment, cb),
                cb -> local.createDepartment(name, code, headOfDepartment, cb),
                cache::upsertDepartment,
                created -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("name", name);
                    payload.put("code", code);
                    payload.put("headOfDepartment", headOfDepartment);
                    store.enqueue(PendingOp.create(SyncOpType.CREATE_DEPARTMENT, created.getId(), payload));
                },
                callback);
    }

    @Override
    public void createAcademicSetup(String departmentName, String shift, int semesterNumber, String sectionName,
                                    String courseName, String courseCode, ApiCallback<Department> callback) {
        local.createAcademicSetup(departmentName, shift, semesterNumber, sectionName, courseName, courseCode, callback);
    }

    @Override
    public void importAcademicStructure(com.example.studentattendance.utils.AcademicExcelParser.Result parsed,
                                        ApiCallback<com.example.studentattendance.model.ImportResult> callback) {
        local.importAcademicStructure(parsed, callback);
    }

    @Override
    public void updateAcademicSetup(String subjectId, String departmentName, String shift, int semesterNumber,
                                    String sectionName, String courseName, String courseCode,
                                    ApiCallback<Department> callback) {
        local.updateAcademicSetup(subjectId, departmentName, shift, semesterNumber, sectionName,
                courseName, courseCode, callback);
    }

    @Override
    public void createSemester(String departmentId, String name, int semesterNumber, ApiCallback<Semester> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.createSemester(store.resolveId(departmentId), name, semesterNumber, cb),
                cb -> local.createSemester(departmentId, name, semesterNumber, cb),
                cache::upsertSemester,
                created -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("departmentId", departmentId);
                    payload.put("name", name);
                    payload.put("semesterNumber", semesterNumber);
                    store.enqueue(PendingOp.create(SyncOpType.CREATE_SEMESTER, created.getId(), payload));
                },
                callback);
    }

    @Override
    public void createSection(String semesterId, String name, ApiCallback<Section> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.createSection(store.resolveId(semesterId), name, cb),
                cb -> local.createSection(semesterId, name, cb),
                cache::upsertSection,
                created -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("semesterId", semesterId);
                    payload.put("name", name);
                    store.enqueue(PendingOp.create(SyncOpType.CREATE_SECTION, created.getId(), payload));
                },
                callback);
    }

    @Override
    public void createSubject(String departmentId, String name, String code, ApiCallback<Subject> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.createSubject(store.resolveId(departmentId), name, code, cb),
                cb -> local.createSubject(departmentId, name, code, cb),
                cache::upsertSubject,
                created -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("departmentId", departmentId);
                    payload.put("name", name);
                    payload.put("code", code);
                    store.enqueue(PendingOp.create(SyncOpType.CREATE_SUBJECT, created.getId(), payload));
                },
                callback);
    }

    @Override
    public void updateDepartment(String departmentId, String name, String code, String headOfDepartment,
                                  Boolean archived, ApiCallback<Department> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.updateDepartment(store.resolveId(departmentId), name, code, headOfDepartment, archived, cb),
                cb -> local.updateDepartment(departmentId, name, code, headOfDepartment, archived, cb),
                cache::upsertDepartment,
                ignored -> enqueueUpdate(SyncOpType.UPDATE_DEPARTMENT, departmentId, name, code, headOfDepartment, archived),
                callback);
    }

    @Override
    public void deleteDepartment(String departmentId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.deleteDepartment(store.resolveId(departmentId), cb),
                cb -> local.deleteDepartment(departmentId, cb),
                () -> enqueueId(SyncOpType.DELETE_DEPARTMENT, departmentId),
                callback);
    }

    @Override
    public void updateSemester(String semesterId, String name, Integer semesterNumber, Boolean archived,
                                ApiCallback<Semester> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.updateSemester(store.resolveId(semesterId), name, semesterNumber, archived, cb),
                cb -> local.updateSemester(semesterId, name, semesterNumber, archived, cb),
                cache::upsertSemester,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", semesterId);
                    payload.put("name", name);
                    payload.put("semesterNumber", semesterNumber);
                    payload.put("archived", archived);
                    store.enqueue(PendingOp.create(SyncOpType.UPDATE_SEMESTER, null, payload));
                },
                callback);
    }

    @Override
    public void deleteSemester(String semesterId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.deleteSemester(store.resolveId(semesterId), cb),
                cb -> local.deleteSemester(semesterId, cb),
                () -> enqueueId(SyncOpType.DELETE_SEMESTER, semesterId),
                callback);
    }

    @Override
    public void updateSection(String sectionId, String name, Boolean archived, ApiCallback<Section> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.updateSection(store.resolveId(sectionId), name, archived, cb),
                cb -> local.updateSection(sectionId, name, archived, cb),
                cache::upsertSection,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", sectionId);
                    payload.put("name", name);
                    payload.put("archived", archived);
                    store.enqueue(PendingOp.create(SyncOpType.UPDATE_SECTION, null, payload));
                },
                callback);
    }

    @Override
    public void deleteSection(String sectionId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.deleteSection(store.resolveId(sectionId), cb),
                cb -> local.deleteSection(sectionId, cb),
                () -> enqueueId(SyncOpType.DELETE_SECTION, sectionId),
                callback);
    }

    @Override
    public void updateSubject(String subjectId, String name, String code, Boolean archived,
                               ApiCallback<Subject> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.updateSubject(store.resolveId(subjectId), name, code, archived, cb),
                cb -> local.updateSubject(subjectId, name, code, archived, cb),
                cache::upsertSubject,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", subjectId);
                    payload.put("name", name);
                    payload.put("code", code);
                    payload.put("archived", archived);
                    store.enqueue(PendingOp.create(SyncOpType.UPDATE_SUBJECT, null, payload));
                },
                callback);
    }

    @Override
    public void deleteSubject(String subjectId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.deleteSubject(store.resolveId(subjectId), cb),
                cb -> local.deleteSubject(subjectId, cb),
                () -> enqueueId(SyncOpType.DELETE_SUBJECT, subjectId),
                callback);
    }

    @Override
    public void getLectures(String sectionId, String subjectId, ApiCallback<List<Lecture>> callback) {
        local.getLectures(sectionId, subjectId, callback);
    }

    @Override
    public void createLecture(String sectionId, String subjectId, int lectureNumber, String title,
                              ApiCallback<Lecture> callback) {
        local.createLecture(sectionId, subjectId, lectureNumber, title, callback);
    }

    @Override
    public void deleteLecture(String lectureId, ApiCallback<Void> callback) {
        local.deleteLecture(lectureId, callback);
    }

    private void enqueueUpdate(String type, String id, String name, String code, String head, Boolean archived) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("name", name);
        payload.put("code", code);
        payload.put("headOfDepartment", head);
        payload.put("archived", archived);
        store.enqueue(PendingOp.create(type, null, payload));
    }

    private void enqueueId(String type, String id) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        store.enqueue(PendingOp.create(type, null, payload));
    }
}
