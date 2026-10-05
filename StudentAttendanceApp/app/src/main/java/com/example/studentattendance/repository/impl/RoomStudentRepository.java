package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ValidationUtils;

public class RoomStudentRepository implements StudentRepository {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomStudentRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void getStudents(String departmentId, String semesterId, String sectionId,
                             ApiCallback<List<Student>> callback) {
        LocalExec.io(() -> {
            if (sectionId == null) {
                throw new LocalDataException("Please select a section first.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireSection(ownerId, sectionId);
            List<Student> result = new ArrayList<>();
            for (StudentEntity entity : db.studentDao().listBySection(ownerId, sectionId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void addStudent(Student student, ApiCallback<Student> callback) {
        LocalExec.io(() -> insertStudent(LocalOwner.requireId(prefs), student), callback);
    }

    @Override
    public void updateStudent(String studentId, String name, String rollNumber, String email, String phone,
                               Boolean archived, ApiCallback<Student> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            StudentEntity entity = requireStudent(ownerId, studentId);
            if (name != null && !name.trim().isEmpty()) {
                entity.name = name.trim();
            }
            if (rollNumber != null && !rollNumber.trim().isEmpty()) {
                String roll = rollNumber.trim();
                StudentEntity clash = db.studentDao().findByRollInSection(ownerId, entity.sectionId, roll);
                if (clash != null && !clash.id.equals(entity.id)) {
                    throw new LocalDataException("A student with this roll number already exists in this class.");
                }
                entity.rollNumber = roll;
            }
            if (email != null) {
                entity.email = email.trim();
            }
            if (phone != null) {
                entity.phone = phone.trim();
            }
            if (archived != null) {
                entity.archived = archived;
            }
            db.studentDao().update(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void transferStudent(String studentId, String sectionId, ApiCallback<Student> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            StudentEntity entity = requireStudent(ownerId, studentId);
            SectionEntity section = requireSection(ownerId, sectionId);
            SemesterEntity semester = db.semesterDao().findOwned(section.semesterId, ownerId);
            if (semester == null) {
                throw new LocalDataException("Semester not found.");
            }
            StudentEntity clash = db.studentDao().findByRollInSection(ownerId, sectionId, entity.rollNumber);
            if (clash != null && !clash.id.equals(entity.id)) {
                throw new LocalDataException("A student with this roll number already exists in this class.");
            }
            entity.sectionId = section.id;
            entity.semesterId = semester.id;
            entity.departmentId = semester.departmentId;
            db.studentDao().update(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void deleteStudent(String studentId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            requireStudent(ownerId, studentId);
            db.studentDao().delete(studentId, ownerId);
        }, callback);
    }

    @Override
    public void importStudents(List<Student> students, ApiCallback<ImportResult> callback) {
        LocalExec.io(() -> {
            if (students == null || students.isEmpty()) {
                throw new LocalDataException("No valid student rows were found in the selected file.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            ImportResult result = new ImportResult();
            int added = 0;
            for (Student student : students) {
                try {
                    insertStudent(ownerId, student);
                    added++;
                } catch (LocalDataException e) {
                    if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                        result.setDuplicates(result.getDuplicates() + 1);
                    } else {
                        result.setInvalid(result.getInvalid() + 1);
                    }
                }
            }
            result.setImported(added);
            return result;
        }, callback);
    }

    @Override
    public void getAllStudents(ApiCallback<List<Student>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<Student> result = new ArrayList<>();
            for (StudentEntity entity : db.studentDao().listAll(ownerId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    private Student insertStudent(String ownerId, Student student) {
        if (student == null || student.getName() == null || student.getName().trim().isEmpty()) {
            throw new LocalDataException("Student name is required.");
        }
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            throw new LocalDataException("Roll number is required.");
        }
        if (student.getSectionId() == null) {
            throw new LocalDataException("Please select a section first.");
        }
        SectionEntity section = requireSection(ownerId, student.getSectionId());
        SemesterEntity semester = db.semesterDao().findOwned(section.semesterId, ownerId);
        if (semester == null) {
            throw new LocalDataException("Semester not found.");
        }
        String roll = student.getRollNumber().trim();
        if (db.studentDao().findByRollInSection(ownerId, section.id, roll) != null) {
            throw new LocalDataException("A student with this roll number already exists in this class.");
        }
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()
                && !ValidationUtils.isValidEmail(student.getEmail())) {
            throw new LocalDataException("Please enter a valid student email address.");
        }
        StudentEntity entity = new StudentEntity();
        entity.id = student.getId() != null ? student.getId() : "st_" + UUID.randomUUID();
        entity.ownerUserId = ownerId;
        entity.rollNumber = roll;
        entity.name = student.getName().trim();
        entity.departmentId = semester.departmentId;
        entity.semesterId = semester.id;
        entity.sectionId = section.id;
        entity.email = student.getEmail();
        entity.phone = student.getPhone();
        db.studentDao().insert(entity);
        return entity.toModel();
    }

    private StudentEntity requireStudent(String ownerId, String studentId) {
        StudentEntity entity = db.studentDao().findOwned(studentId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Student not found.");
        }
        return entity;
    }

    private SectionEntity requireSection(String ownerId, String sectionId) {
        SectionEntity entity = db.sectionDao().findOwned(sectionId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Section not found.");
        }
        return entity;
    }
}
