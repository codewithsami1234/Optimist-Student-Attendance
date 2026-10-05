package com.example.studentattendance.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AttendanceImportParser;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.HistoryFilterOptions;
import com.example.studentattendance.utils.SemesterAttendance;

/**
 * Loads saved attendance sessions from Room and applies History filters by
 * academic IDs (AND logic). Filter state survives refresh and returning from
 * View History.
 */
public class AttendanceHistoryViewModel extends AndroidViewModel {

    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceAdjustmentRepository adjustmentRepository;

    private final MutableLiveData<NetworkResult<List<AttendanceSession>>> sessions = new MutableLiveData<>();
    private final MutableLiveData<List<ClassSetup>> classSetups = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<SemesterAttendance.Review>>> semesterReview =
            new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<ImportResult>> importResult = new MutableLiveData<>();

    private final HistoryFilter filter = new HistoryFilter();
    private List<ClassSetup> setups = new ArrayList<>();

    public AttendanceHistoryViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        attendanceRepository = provider.getAttendanceRepository();
        departmentRepository = provider.getDepartmentRepository();
        adjustmentRepository = provider.getAttendanceAdjustmentRepository();
    }

    public LiveData<NetworkResult<List<AttendanceSession>>> getSessions() {
        return sessions;
    }

    public LiveData<List<ClassSetup>> getClassSetups() {
        return classSetups;
    }

    public LiveData<NetworkResult<List<SemesterAttendance.Review>>> getSemesterReview() {
        return semesterReview;
    }

    public LiveData<NetworkResult<ImportResult>> getImportResult() {
        return importResult;
    }

    public HistoryFilter getFilter() {
        return filter;
    }

    public HistoryFilterOptions.Snapshot options() {
        return HistoryFilterOptions.from(setups, filter.departmentId, filter.semesterId, filter.sectionId);
    }

    public HistoryFilterOptions.Snapshot options(String departmentId, String semesterId, String sectionId) {
        return HistoryFilterOptions.from(setups, departmentId, semesterId, sectionId);
    }

    public boolean hasActiveFilters() {
        return filter.hasAny();
    }

    public boolean hasClassScope() {
        return filter.departmentId != null && filter.semesterId != null && filter.subjectId != null;
    }

    public void loadFilterOptions() {
        departmentRepository.getClassSetups(new ApiCallback<List<ClassSetup>>() {
            @Override
            public void onSuccess(List<ClassSetup> data) {
                setups = data == null ? new ArrayList<>() : data;
                classSetups.setValue(setups);
            }

            @Override
            public void onError(String message) {
                setups = new ArrayList<>();
                classSetups.setValue(setups);
            }
        });
    }

    public void loadSessions() {
        sessions.setValue(NetworkResult.loading());
        attendanceRepository.getSessions(filter.departmentId, filter.semesterId, filter.sectionId,
                filter.subjectId, filter.fromDate, filter.toDate,
                new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> data) {
                        sessions.setValue(NetworkResult.success(HistoryFilter.apply(data, filter)));
                    }

                    @Override
                    public void onError(String message) {
                        sessions.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void applyFilters(String departmentId, String semesterId, String sectionId,
                             String subjectId, String courseCode, String fromDate, String toDate) {
        filter.departmentId = HistoryFilter.emptyToNull(departmentId);
        filter.semesterId = HistoryFilter.emptyToNull(semesterId);
        filter.sectionId = HistoryFilter.emptyToNull(sectionId);
        filter.subjectId = HistoryFilter.emptyToNull(subjectId);
        filter.courseCode = HistoryFilter.normalizeCode(courseCode);
        filter.fromDate = HistoryFilter.emptyToNull(fromDate);
        filter.toDate = HistoryFilter.emptyToNull(toDate);
        if (filter.subjectId != null) {
            for (ClassSetup setup : setups) {
                if (setup != null && setup.getSubject() != null
                        && filter.subjectId.equals(setup.getSubject().getId())) {
                    if (filter.sectionId == null && setup.getSection() != null) {
                        filter.sectionId = setup.getSection().getId();
                    }
                    break;
                }
            }
        }
        loadSessions();
    }

    public void clearFilters() {
        filter.departmentId = null;
        filter.semesterId = null;
        filter.sectionId = null;
        filter.subjectId = null;
        filter.courseCode = null;
        filter.fromDate = null;
        filter.toDate = null;
        loadSessions();
    }

    public void loadSemesterReview() {
        if (!hasClassScope()) {
            semesterReview.setValue(NetworkResult.error(
                    "Select department, semester, and course first."));
            return;
        }
        semesterReview.setValue(NetworkResult.loading());
        attendanceRepository.getSessions(filter.departmentId, filter.semesterId, filter.sectionId,
                filter.subjectId, null, null, new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> data) {
                        List<AttendanceSession> scoped = HistoryFilter.apply(data, classOnlyFilter());
                        adjustmentRepository.getAll(new ApiCallback<List<AttendanceAdjustment>>() {
                            @Override
                            public void onSuccess(List<AttendanceAdjustment> adjustments) {
                                Map<String, Integer> deltas = new HashMap<>();
                                if (adjustments != null) {
                                    for (AttendanceAdjustment adjustment : adjustments) {
                                        if (adjustment != null && adjustment.getStudentId() != null) {
                                            deltas.merge(adjustment.getStudentId(), adjustment.getDelta(),
                                                    Integer::sum);
                                        }
                                    }
                                }
                                semesterReview.setValue(NetworkResult.success(
                                        SemesterAttendance.reviews(scoped, deltas,
                                                AttendancePolicy.DEFAULT_LOW_THRESHOLD)));
                            }

                            @Override
                            public void onError(String message) {
                                semesterReview.setValue(NetworkResult.success(
                                        SemesterAttendance.reviews(scoped, null,
                                                AttendancePolicy.DEFAULT_LOW_THRESHOLD)));
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        semesterReview.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void loadSemesterSessions(ApiCallback<List<AttendanceSession>> callback) {
        if (!hasClassScope()) {
            callback.onError("Select department, semester, and course first.");
            return;
        }
        attendanceRepository.getSessions(filter.departmentId, filter.semesterId, filter.sectionId,
                filter.subjectId, null, null, new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> data) {
                        callback.onSuccess(HistoryFilter.apply(data, classOnlyFilter()));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    /**
     * Loads sessions for History export. Department and both dates are required.
     * Shift, section, course, and course code are optional (null = no filter).
     */
    public void loadSessionsForExport(String departmentId, String semesterId, String sectionId,
                                      String subjectId, String courseCode, String shift,
                                      String fromDate, String toDate,
                                      ApiCallback<List<AttendanceSession>> callback) {
        if (HistoryFilter.emptyToNull(departmentId) == null
                || HistoryFilter.emptyToNull(fromDate) == null
                || HistoryFilter.emptyToNull(toDate) == null) {
            callback.onError("Select department and both dates.");
            return;
        }
        if (fromDate.compareTo(toDate) > 0) {
            callback.onError("Date From cannot be after Date To.");
            return;
        }
        String scopedSemester = HistoryFilter.emptyToNull(semesterId);
        String scopedSection = HistoryFilter.emptyToNull(sectionId);
        String scopedSubject = HistoryFilter.emptyToNull(subjectId);
        attendanceRepository.getSessions(departmentId, scopedSemester, scopedSection, scopedSubject,
                fromDate, toDate,
                new ApiCallback<List<AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<AttendanceSession> data) {
                        HistoryFilter scoped = new HistoryFilter(
                                departmentId, scopedSemester, scopedSection, scopedSubject,
                                courseCode, fromDate, toDate, shift);
                        scoped.bindShiftDepartments(setups);
                        callback.onSuccess(HistoryFilter.apply(data, scoped));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    public void loadAdjustmentDeltas(ApiCallback<Map<String, Integer>> callback) {
        adjustmentRepository.getAll(new ApiCallback<List<AttendanceAdjustment>>() {
            @Override
            public void onSuccess(List<AttendanceAdjustment> adjustments) {
                Map<String, Integer> deltas = new HashMap<>();
                if (adjustments != null) {
                    for (AttendanceAdjustment adjustment : adjustments) {
                        if (adjustment != null && adjustment.getStudentId() != null) {
                            deltas.merge(adjustment.getStudentId(), adjustment.getDelta(), Integer::sum);
                        }
                    }
                }
                callback.onSuccess(deltas);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(new HashMap<>());
            }
        });
    }

    public void importAttendance(List<AttendanceImportParser.Row> rows) {
        if (!hasClassScope()) {
            importResult.setValue(NetworkResult.error(
                    "Select department, semester, and course first."));
            return;
        }
        String sectionId = filter.sectionId;
        if (sectionId == null) {
            for (ClassSetup setup : setups) {
                if (setup != null && setup.getSubject() != null
                        && filter.subjectId.equals(setup.getSubject().getId())
                        && setup.getSection() != null) {
                    sectionId = setup.getSection().getId();
                    break;
                }
            }
        }
        if (sectionId == null) {
            importResult.setValue(NetworkResult.error("Select a section or course first."));
            return;
        }
        importResult.setValue(NetworkResult.loading());
        attendanceRepository.importAttendance(filter.departmentId, filter.semesterId, sectionId,
                filter.subjectId, rows, new ApiCallback<ImportResult>() {
                    @Override
                    public void onSuccess(ImportResult data) {
                        importResult.setValue(NetworkResult.success(data));
                        loadSessions();
                    }

                    @Override
                    public void onError(String message) {
                        importResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    private HistoryFilter classOnlyFilter() {
        HistoryFilter scoped = new HistoryFilter();
        scoped.departmentId = filter.departmentId;
        scoped.semesterId = filter.semesterId;
        scoped.sectionId = filter.sectionId;
        scoped.subjectId = filter.subjectId;
        scoped.courseCode = filter.courseCode;
        return scoped;
    }
}
