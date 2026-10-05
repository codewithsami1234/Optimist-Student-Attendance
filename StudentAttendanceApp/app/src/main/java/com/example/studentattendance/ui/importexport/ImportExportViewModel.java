package com.example.studentattendance.ui.importexport;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.CsvParseResult;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.service.CsvExcelService;
import com.example.studentattendance.service.ExcelService;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AcademicExcelParser;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.SemesterAttendance;

public class ImportExportViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final ExcelService excelService;

    private final MutableLiveData<NetworkResult<Integer>> operationResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<CsvParseResult>> parseResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<ImportResult>> importResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Department>>> departments = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Semester>>> semesters = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Section>>> sections = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Subject>>> subjects = new MutableLiveData<>();

    public static final class ImportSectionOption {
        public final Section section;
        public final Semester semester;
        public final String label;

        public ImportSectionOption(Section section, Semester semester, String label) {
            this.section = section;
            this.semester = semester;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final MutableLiveData<NetworkResult<List<ImportSectionOption>>> importSections =
            new MutableLiveData<>();

    private CsvParseResult lastParse;

    public ImportExportViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        studentRepository = provider.getStudentRepository();
        attendanceRepository = provider.getAttendanceRepository();
        departmentRepository = provider.getDepartmentRepository();
        adjustmentRepository = provider.getAttendanceAdjustmentRepository();
        excelService = provider.getExcelService();
    }

    public LiveData<NetworkResult<Integer>> getOperationResult() { return operationResult; }
    public LiveData<NetworkResult<CsvParseResult>> getParseResult() { return parseResult; }
    public LiveData<NetworkResult<ImportResult>> getImportResult() { return importResult; }
    public LiveData<NetworkResult<List<Department>>> getDepartments() { return departments; }
    public LiveData<NetworkResult<List<Semester>>> getSemesters() { return semesters; }
    public LiveData<NetworkResult<List<Section>>> getSections() { return sections; }
    public LiveData<NetworkResult<List<Subject>>> getSubjects() { return subjects; }
    public LiveData<NetworkResult<List<ImportSectionOption>>> getImportSections() {
        return importSections;
    }

    public void loadDepartments() {
        departmentRepository.getDepartments(new ApiCallback<List<Department>>() {
            @Override
            public void onSuccess(List<Department> data) {
                departments.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                departments.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSemesters(String departmentId) {
        departmentRepository.getSemesters(departmentId, new ApiCallback<List<Semester>>() {
            @Override
            public void onSuccess(List<Semester> data) {
                semesters.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                semesters.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSections(String semesterId) {
        departmentRepository.getSections(semesterId, new ApiCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {
                sections.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                sections.setValue(NetworkResult.error(message));
            }
        });
    }

    /**
     * Loads unique sections for a department (across semesters) for the import
     * dialog that no longer shows a semester picker. Semester is still attached
     * to each option for Room import.
     */
    public void loadImportSectionsForDepartment(String departmentId) {
        importSections.setValue(NetworkResult.loading());
        departmentRepository.getClassSetups(new ApiCallback<List<ClassSetup>>() {
            @Override
            public void onSuccess(List<ClassSetup> data) {
                Map<String, ImportSectionOption> unique = new LinkedHashMap<>();
                if (data != null) {
                    for (ClassSetup setup : data) {
                        if (setup == null || setup.getSection() == null
                                || setup.getSection().getId() == null) {
                            continue;
                        }
                        if (departmentId != null && setup.getDepartment() != null
                                && !departmentId.equals(setup.getDepartment().getId())) {
                            continue;
                        }
                        String sectionId = setup.getSection().getId();
                        if (unique.containsKey(sectionId)) {
                            continue;
                        }
                        String sectionName = setup.getSection().getName();
                        if (sectionName == null || sectionName.trim().isEmpty()) {
                            sectionName = "No section";
                        }
                        String semesterName = setup.semesterLabel();
                        String label = semesterName == null || semesterName.isEmpty()
                                ? sectionName
                                : sectionName + " · " + semesterName;
                        unique.put(sectionId, new ImportSectionOption(
                                setup.getSection(), setup.getSemester(), label));
                    }
                }
                importSections.setValue(NetworkResult.success(new ArrayList<>(unique.values())));
            }

            @Override
            public void onError(String message) {
                importSections.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSubjects(String departmentId) {
        departmentRepository.getSubjects(departmentId, new ApiCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> data) {
                subjects.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                subjects.setValue(NetworkResult.error(message));
            }
        });
    }

    public void exportAttendance(Uri destinationUri, String departmentId, String semesterId,
                                  String sectionId, String subjectId, String fromDate, String toDate) {
        operationResult.setValue(NetworkResult.loading());
        attendanceRepository.getSessions(departmentId, semesterId, sectionId, subjectId, fromDate, toDate,
                new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> sessions) {
                        studentRepository.getAllStudents(new ApiCallback<List<Student>>() {
                            @Override
                            public void onSuccess(List<Student> students) {
                                java.util.List<StudentReport> reports = buildReports(students, sessions);
                                applyAdjustmentsThenExport(destinationUri, sessions, reports);
                            }

                            @Override
                            public void onError(String message) {
                                if (excelService instanceof CsvExcelService) {
                                    ((CsvExcelService) excelService).exportAttendanceWorkbook(
                                            destinationUri, sessions, null, wrapCount());
                                } else {
                                    excelService.exportAttendance(destinationUri, sessions, wrapCount());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        operationResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void exportAttendanceSummary(Uri destinationUri) {
        operationResult.setValue(NetworkResult.loading());
        attendanceRepository.getSessionsForReports(new ApiCallback<List<AttendanceSession>>() {
            @Override
            public void onSuccess(List<AttendanceSession> sessions) {
                studentRepository.getAllStudents(new ApiCallback<List<Student>>() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        java.util.Map<String, StudentReport> map = new java.util.LinkedHashMap<>();
                        if (students != null) {
                            for (Student s : students) {
                                map.put(s.getId(), new StudentReport(s.getId(), s.getName(), s.getRollNumber()));
                            }
                        }
                        if (sessions != null) {
                            for (AttendanceSession session : sessions) {
                                for (com.example.studentattendance.model.AttendanceRecord record : session.getRecords()) {
                                    StudentReport report = map.computeIfAbsent(record.getStudentId(),
                                            k -> new StudentReport(record.getStudentId(),
                                                    record.getStudentName(), record.getRollNumber()));
                                    report.increment(record.getStatus());
                                }
                            }
                        }
                        java.util.List<StudentReport> reports = new java.util.ArrayList<>(map.values());
                        applyAdjustments(reports, () ->
                                excelService.exportAttendanceSummary(destinationUri, reports, wrapCount()));
                    }

                    @Override
                    public void onError(String message) {
                        operationResult.setValue(NetworkResult.error(message));
                    }
                });
            }

            @Override
            public void onError(String message) {
                operationResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void fetchClassSetups(ApiCallback<List<ClassSetup>> callback) {
        departmentRepository.getClassSetups(callback);
    }

    /**
     * Read-only attendance load for Profile export. Dates may be null to include
     * every saved session in the selected class.
     */
    public void loadScopedSessionsForExport(String departmentId, String semesterId, String sectionId,
                                            String subjectId, String courseCode, String shift,
                                            String fromDate, String toDate, List<ClassSetup> setups,
                                            ApiCallback<List<AttendanceSession>> callback) {
        attendanceRepository.getSessions(departmentId, semesterId, sectionId, subjectId, fromDate, toDate,
                new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> data) {
                        HistoryFilter scoped = new HistoryFilter(
                                departmentId, semesterId, sectionId, subjectId,
                                courseCode, fromDate, toDate, shift);
                        scoped.bindShiftDepartments(setups);
                        List<AttendanceSession> matched = HistoryFilter.apply(data, scoped);
                        if (SemesterAttendance.reviews(matched, null,
                                com.example.studentattendance.utils.AttendancePolicy.DEFAULT_LOW_THRESHOLD)
                                .isEmpty()) {
                            callback.onSuccess(new ArrayList<>());
                            return;
                        }
                        callback.onSuccess(matched);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    public void loadAdjustmentDeltas(ApiCallback<Map<String, Integer>> callback) {
        adjustmentRepository.getAll(new ApiCallback<List<com.example.studentattendance.model.AttendanceAdjustment>>() {
            @Override
            public void onSuccess(List<com.example.studentattendance.model.AttendanceAdjustment> data) {
                Map<String, Integer> deltas = new LinkedHashMap<>();
                if (data != null) {
                    for (com.example.studentattendance.model.AttendanceAdjustment adjustment : data) {
                        if (adjustment == null || adjustment.getStudentId() == null) {
                            continue;
                        }
                        Integer current = deltas.get(adjustment.getStudentId());
                        deltas.put(adjustment.getStudentId(),
                                (current == null ? 0 : current) + adjustment.getDelta());
                    }
                }
                callback.onSuccess(deltas);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(new LinkedHashMap<>());
            }
        });
    }

    public void writeConsolidatedExport(Uri destinationUri, AttendanceExportRows.ReportMeta meta,
                                        List<AttendanceSession> sessions, Map<String, Integer> deltas) {
        operationResult.setValue(NetworkResult.loading());
        excelService.exportConsolidatedAttendanceReport(destinationUri, meta, sessions, deltas, wrapCount());
    }

    public void exportStudents(Uri destinationUri) {
        exportStudents(destinationUri, false);
    }

    public void exportStudents(Uri destinationUri, boolean csv) {
        operationResult.setValue(NetworkResult.loading());
        studentRepository.getAllStudents(new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> students) {
                if (csv) {
                    excelService.exportStudentsCsv(destinationUri, students, wrapCount());
                } else {
                    excelService.exportStudents(destinationUri, students, wrapCount());
                }
            }

            @Override
            public void onError(String message) {
                operationResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void downloadTemplate(Uri destinationUri) {
        downloadTemplate(destinationUri, false);
    }

    public void downloadTemplate(Uri destinationUri, boolean csv) {
        operationResult.setValue(NetworkResult.loading());
        if (csv) {
            excelService.writeStudentTemplateCsv(destinationUri, wrapCount());
        } else {
            excelService.writeStudentTemplate(destinationUri, wrapCount());
        }
    }

    /**
     * Profile → Import Excel. The workbook itself supplies department, shift,
     * semester, section, course, and students. Nothing is deleted.
     */
    public void importAcademicExcel(Uri sourceUri) {
        importResult.setValue(NetworkResult.loading());
        excelService.parseAcademicImport(sourceUri, new ApiCallback<AcademicExcelParser.Result>() {
            @Override
            public void onSuccess(AcademicExcelParser.Result data) {
                departmentRepository.importAcademicStructure(data, new ApiCallback<ImportResult>() {
                    @Override
                    public void onSuccess(ImportResult result) {
                        importResult.setValue(NetworkResult.success(result));
                    }

                    @Override
                    public void onError(String message) {
                        importResult.setValue(NetworkResult.error(message));
                    }
                });
            }

            @Override
            public void onError(String message) {
                importResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void parseImport(Uri sourceUri, String departmentId, String semesterId, String sectionId) {
        parseResult.setValue(NetworkResult.loading());
        excelService.parseStudentImport(sourceUri, departmentId, semesterId, sectionId,
                new ApiCallback<CsvParseResult>() {
                    @Override
                    public void onSuccess(CsvParseResult data) {
                        lastParse = data;
                        parseResult.setValue(NetworkResult.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        parseResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void confirmImport() {
        if (lastParse == null || lastParse.validStudents.isEmpty()) {
            importResult.setValue(NetworkResult.error("There are no valid rows to import."));
            return;
        }
        importResult.setValue(NetworkResult.loading());
        studentRepository.importStudents(lastParse.validStudents, new ApiCallback<ImportResult>() {
            @Override
            public void onSuccess(ImportResult data) {
                importResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                importResult.setValue(NetworkResult.error(message));
            }
        });
    }

    private java.util.List<StudentReport> buildReports(List<Student> students, List<AttendanceSession> sessions) {
        java.util.Map<String, StudentReport> map = new java.util.LinkedHashMap<>();
        if (students != null) {
            for (Student s : students) {
                map.put(s.getId(), new StudentReport(s.getId(), s.getName(), s.getRollNumber()));
            }
        }
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (session.getRecords() == null) {
                    continue;
                }
                for (com.example.studentattendance.model.AttendanceRecord record : session.getRecords()) {
                    StudentReport report = map.computeIfAbsent(record.getStudentId(),
                            k -> new StudentReport(record.getStudentId(),
                                    record.getStudentName(), record.getRollNumber()));
                    report.increment(record.getStatus());
                }
            }
        }
        return new java.util.ArrayList<>(map.values());
    }

    private void applyAdjustmentsThenExport(Uri destinationUri, List<AttendanceSession> sessions,
                                            java.util.List<StudentReport> reports) {
        applyAdjustments(reports, () -> {
            if (excelService instanceof CsvExcelService) {
                ((CsvExcelService) excelService).exportAttendanceWorkbook(
                        destinationUri, sessions, reports, wrapCount());
            } else {
                excelService.exportAttendance(destinationUri, sessions, wrapCount());
            }
        });
    }

    private void applyAdjustments(java.util.List<StudentReport> reports, Runnable then) {
        adjustmentRepository.getAll(new ApiCallback<java.util.List<com.example.studentattendance.model.AttendanceAdjustment>>() {
            @Override
            public void onSuccess(java.util.List<com.example.studentattendance.model.AttendanceAdjustment> data) {
                if (data != null && reports != null) {
                    java.util.Map<String, Integer> deltas = new java.util.HashMap<>();
                    for (com.example.studentattendance.model.AttendanceAdjustment adj : data) {
                        if (adj.getStudentId() == null) {
                            continue;
                        }
                        Integer current = deltas.get(adj.getStudentId());
                        deltas.put(adj.getStudentId(), (current == null ? 0 : current) + adj.getDelta());
                    }
                    for (StudentReport report : reports) {
                        Integer delta = deltas.get(report.getStudentId());
                        if (delta != null) {
                            report.setAdjustmentDelta(delta);
                        }
                    }
                }
                then.run();
            }

            @Override
            public void onError(String message) {
                then.run();
            }
        });
    }

    private ApiCallback<Integer> wrapCount() {
        return new ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                operationResult.setValue(NetworkResult.success(count));
            }

            @Override
            public void onError(String message) {
                operationResult.setValue(NetworkResult.error(message));
            }
        };
    }
}
