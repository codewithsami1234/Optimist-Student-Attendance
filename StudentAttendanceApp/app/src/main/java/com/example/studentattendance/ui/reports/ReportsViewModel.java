package com.example.studentattendance.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.R;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.HistoryFilterOptions;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ReportAnalytics;
import com.example.studentattendance.utils.SemesterReport;

/**
 * Loads Room sessions, roster, and class setups, then applies independent
 * Department / Section / Course Code filters through {@link ReportAnalytics}.
 */
public class ReportsViewModel extends AndroidViewModel {

    public enum SortMode { NAME, PERCENTAGE, ROLL, PRESENT, ABSENT }

    public enum StatusFilter { ALL, BELOW_THRESHOLD, AT_OR_ABOVE }

    public static class FilterOption {
        public final String id;
        public final String label;

        public FilterOption(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static class FilterOptions {
        public List<FilterOption> departments = new ArrayList<>();
        public List<FilterOption> semesters = new ArrayList<>();
        public List<FilterOption> sections = new ArrayList<>();
        public List<FilterOption> subjects = new ArrayList<>();
        public List<FilterOption> courseCodes = new ArrayList<>();
    }

    public static class ReportsData {
        public double overallRate;
        public int totalSessions;
        public int totalStudents;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int leaveCount;
        public int belowCount;
        public int atOrAboveCount;
        public double lowAttendanceThreshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;
        public List<StudentReport> studentReports = new ArrayList<>();
        public List<AttendanceSession> rawSessions = new ArrayList<>();
        public List<com.example.studentattendance.model.DailyTrend> weeklyTrend = new ArrayList<>();
        public List<com.example.studentattendance.model.DailyTrend> monthlyTrend = new ArrayList<>();
        public FilterOptions filterOptions = new FilterOptions();
        public com.example.studentattendance.utils.SemesterReport.Snapshot semester;
        public String headerDepartment = "All Departments";
        public String headerShift = "All Shifts";
        public String headerSemester = "All Semesters";
        public String headerSection = "All Sections";
        public String headerCourse = "All Courses";
        public String headerCourseCode = "All Course Codes";
        public String reportingPeriod = "";
        public String highestLabel = "";
        public String lowestLabel = "";
        public boolean hasAttendance;
        public boolean statusFilterEmptyBecauseAllAbove;
    }

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final PreferenceManager preferenceManager;
    private final MutableLiveData<NetworkResult<ReportsData>> reportsData = new MutableLiveData<>();

    private List<AttendanceSession> allSessions = new ArrayList<>();
    private List<Student> allStudents = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private List<ClassSetup> setups = new ArrayList<>();
    private FilterOptions filterOptions = new FilterOptions();
    private Map<String, Integer> adjustmentDeltas = new LinkedHashMap<>();

    private String filterDepartmentId;
    private String filterShift;
    private String filterSemesterId;
    private String filterSectionId;
    private String filterSubjectId;
    private String filterCourseCode;
    private String filterFromDate;
    private String filterToDate;
    private String searchQuery = "";
    private SortMode sortMode = SortMode.PERCENTAGE;
    private StatusFilter statusFilter = StatusFilter.ALL;
    private int loadToken;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        attendanceRepository = provider.getAttendanceRepository();
        studentRepository = provider.getStudentRepository();
        departmentRepository = provider.getDepartmentRepository();
        adjustmentRepository = provider.getAttendanceAdjustmentRepository();
        preferenceManager = new PreferenceManager(application);
    }

    public LiveData<NetworkResult<ReportsData>> getReportsData() {
        return reportsData;
    }

    public String getFilterDepartmentId() { return filterDepartmentId; }
    public String getFilterShift() { return filterShift; }
    public String getFilterSemesterId() { return filterSemesterId; }
    public String getFilterSectionId() { return filterSectionId; }
    public String getFilterSubjectId() { return filterSubjectId; }
    public String getFilterCourseCode() { return filterCourseCode; }
    public String getFilterFromDate() { return filterFromDate; }
    public String getFilterToDate() { return filterToDate; }
    public SortMode getSortMode() { return sortMode; }
    public StatusFilter getStatusFilter() { return statusFilter; }

    public HistoryFilter currentFilter() {
        HistoryFilter filter = new HistoryFilter(filterDepartmentId, filterSemesterId, filterSectionId,
                filterSubjectId, filterCourseCode, filterFromDate, filterToDate, filterShift);
        filter.bindShiftDepartments(setups);
        return filter;
    }

    public void loadReports() {
        loadReports(true);
    }

    /** Reloads Room data without flashing the loading state (keeps scroll/filters). */
    public void refreshReports() {
        loadReports(false);
    }

    private void loadReports(boolean showLoading) {
        final int token = ++loadToken;
        if (showLoading) {
            reportsData.setValue(NetworkResult.loading());
        }
        departmentRepository.getClassSetups(new ApiCallback<List<ClassSetup>>() {
            @Override
            public void onSuccess(List<ClassSetup> data) {
                if (token != loadToken) {
                    return;
                }
                setups = data != null ? data : new ArrayList<>();
                collectFilterOptions(setups);
                loadSessions(token);
            }

            @Override
            public void onError(String message) {
                if (token != loadToken) {
                    return;
                }
                setups = new ArrayList<>();
                collectFilterOptions(null);
                loadSessions(token);
            }
        });
    }

    private void loadSessions(int token) {
        attendanceRepository.getSessionsForReports(new ApiCallback<List<AttendanceSession>>() {
            @Override
            public void onSuccess(List<AttendanceSession> sessions) {
                if (token != loadToken) {
                    return;
                }
                allSessions = sessions != null ? sessions : new ArrayList<>();
                studentRepository.getAllStudents(new ApiCallback<List<Student>>() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        if (token != loadToken) {
                            return;
                        }
                        allStudents = students != null ? students : new ArrayList<>();
                        loadAdjustmentsThenFinish(token);
                    }

                    @Override
                    public void onError(String message) {
                        if (token != loadToken) {
                            return;
                        }
                        allStudents = new ArrayList<>();
                        loadAdjustmentsThenFinish(token);
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (token != loadToken) {
                    return;
                }
                reportsData.setValue(NetworkResult.error(message));
            }
        });
    }

    private void loadAdjustmentsThenFinish(int token) {
        adjustmentRepository.getAll(new ApiCallback<List<AttendanceAdjustment>>() {
            @Override
            public void onSuccess(List<AttendanceAdjustment> data) {
                if (token != loadToken) {
                    return;
                }
                adjustmentDeltas = toDeltaMap(data);
                reportsData.setValue(NetworkResult.success(rebuild()));
            }

            @Override
            public void onError(String message) {
                if (token != loadToken) {
                    return;
                }
                adjustmentDeltas = new LinkedHashMap<>();
                reportsData.setValue(NetworkResult.success(rebuild()));
            }
        });
    }

    public void setFilterDepartment(String departmentId) {
        applyFilters(departmentId, filterShift, null, null, null, null, filterFromDate, filterToDate);
    }

    public void setFilterShift(String shift) {
        applyFilters(filterDepartmentId, shift, filterSemesterId, filterSectionId,
                filterSubjectId, filterCourseCode, filterFromDate, filterToDate);
    }

    public void setFilterSemester(String semesterId) {
        applyFilters(filterDepartmentId, filterShift, semesterId, null, null, null,
                filterFromDate, filterToDate);
    }

    public void setFilterSection(String sectionId) {
        applyFilters(filterDepartmentId, filterShift, filterSemesterId, sectionId, null, null,
                filterFromDate, filterToDate);
    }

    public void setFilterSubject(String subjectId) {
        applyFilters(filterDepartmentId, filterShift, filterSemesterId, filterSectionId,
                subjectId, filterCourseCode, filterFromDate, filterToDate);
    }

    public void setFilterCourseCode(String courseCode) {
        applyFilters(filterDepartmentId, filterShift, filterSemesterId, filterSectionId,
                filterSubjectId, courseCode, filterFromDate, filterToDate);
    }

    public void applyFilters(String departmentId, String shift, String semesterId, String sectionId,
                             String subjectId, String courseCode, String fromDate, String toDate) {
        this.filterDepartmentId = emptyToNull(departmentId);
        this.filterShift = HistoryFilter.normalizeShift(shift);
        this.filterSemesterId = emptyToNull(semesterId);
        this.filterSectionId = emptyToNull(sectionId);
        this.filterSubjectId = emptyToNull(subjectId);
        this.filterCourseCode = emptyToNull(courseCode);
        this.filterFromDate = emptyToNull(fromDate);
        this.filterToDate = emptyToNull(toDate);
        collectFilterOptions(setups);
        publishRebuilt();
    }

    public void clearFilters() {
        filterDepartmentId = null;
        filterShift = null;
        filterSemesterId = null;
        filterSectionId = null;
        filterSubjectId = null;
        filterCourseCode = null;
        filterFromDate = null;
        filterToDate = null;
        searchQuery = "";
        statusFilter = StatusFilter.ALL;
        sortMode = SortMode.PERCENTAGE;
        collectFilterOptions(setups);
        publishRebuilt();
    }

    public void setStatusFilter(StatusFilter filter) {
        this.statusFilter = filter == null ? StatusFilter.ALL : filter;
        publishRebuilt();
    }

    public void setDateRange(String fromDate, String toDate) {
        this.filterFromDate = emptyToNull(fromDate);
        this.filterToDate = emptyToNull(toDate);
        publishRebuilt();
    }

    public void search(String query) {
        searchQuery = query == null ? "" : query.trim();
        publishRebuilt();
    }

    public void setSortMode(SortMode mode) {
        this.sortMode = mode == null ? SortMode.PERCENTAGE : mode;
        publishRebuilt();
    }

    private void publishRebuilt() {
        reportsData.setValue(NetworkResult.success(rebuild()));
    }

    private ReportsData rebuild() {
        HistoryFilter historyFilter = currentFilter();
        SemesterReport.Snapshot semester = SemesterReport.build(
                allSessions,
                adjustmentDeltas,
                historyFilter,
                searchQuery,
                toSemesterSort(sortMode),
                toSemesterStatus(statusFilter));
        String monthStart = semester.periodFrom != null ? semester.periodFrom : DateUtils.startOfCurrentMonth();
        String monthEnd = semester.periodTo != null ? semester.periodTo : DateUtils.endOfCurrentMonth();
        String weekEnd = monthEnd;
        String weekStart = DateUtils.daysAgoFrom(weekEnd, 6);

        List<AttendanceSession> scopedSessions = HistoryFilter.apply(allSessions, historyFilter);
        boolean listNarrowed = statusFilter != StatusFilter.ALL
                || (searchQuery != null && !searchQuery.isEmpty());
        List<AttendanceSession> chartSessions = scopedSessions;
        if (listNarrowed) {
            java.util.HashSet<String> visibleIds = new java.util.HashSet<>();
            if (semester.visibleRows != null) {
                for (SemesterReport.StudentRow row : semester.visibleRows) {
                    if (row != null && row.studentId != null) {
                        visibleIds.add(row.studentId);
                    }
                }
            }
            chartSessions = ReportAnalytics.sessionsForStudentIds(scopedSessions, visibleIds);
        }
        List<com.example.studentattendance.model.DailyTrend> weeklyTrend =
                ReportAnalytics.trendForRange(chartSessions, weekStart, weekEnd);
        List<com.example.studentattendance.model.DailyTrend> monthlyTrend =
                ReportAnalytics.trendForRange(chartSessions, monthStart, monthEnd);

        ReportsData data = new ReportsData();
        data.overallRate = semester.averageRate;
        data.totalSessions = semester.totalSessions;
        data.totalStudents = semester.totalStudents;
        data.presentCount = semester.presentCount;
        data.absentCount = semester.absentCount;
        data.lateCount = semester.lateCount;
        data.leaveCount = semester.leaveCount;
        data.belowCount = semester.belowCount;
        data.atOrAboveCount = semester.atOrAboveCount;
        data.studentReports = toStudentReports(semester.visibleRows);
        data.rawSessions = scopedSessions;
        data.weeklyTrend = weeklyTrend;
        data.monthlyTrend = monthlyTrend;
        data.filterOptions = filterOptions;
        data.lowAttendanceThreshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;
        data.semester = semester;
        data.hasAttendance = semester.hasAttendance;
        data.statusFilterEmptyBecauseAllAbove = semester.statusFilterEmptyBecauseAllAbove;
        data.reportingPeriod = DateUtils.reportingPeriod(semester.periodFrom, semester.periodTo);
        data.headerDepartment = headerLabel(filterOptions.departments, filterDepartmentId,
                getApplication().getString(R.string.reports_header_all_departments));
        data.headerShift = filterShift == null
                ? getApplication().getString(R.string.reports_header_all_shifts)
                : com.example.studentattendance.model.DepartmentShift.label(filterShift);
        data.headerSemester = headerLabel(filterOptions.semesters, filterSemesterId,
                getApplication().getString(R.string.reports_header_all_semesters));
        data.headerSection = headerLabel(filterOptions.sections, filterSectionId,
                getApplication().getString(R.string.reports_header_all_sections));
        data.headerCourse = headerLabel(filterOptions.subjects, filterSubjectId,
                getApplication().getString(R.string.reports_header_all_courses));
        data.headerCourseCode = filterCourseCode == null
                ? getApplication().getString(R.string.reports_header_all_course_codes)
                : filterCourseCode;
        if (semester.highest != null) {
            data.highestLabel = String.format(java.util.Locale.US, "%s · %.1f%%",
                    semester.highest.studentName, semester.highest.attendanceRate);
        }
        if (semester.lowest != null) {
            data.lowestLabel = String.format(java.util.Locale.US, "%s · %.1f%%",
                    semester.lowest.studentName, semester.lowest.attendanceRate);
        }
        return data;
    }

    private void collectFilterOptions(List<ClassSetup> classSetups) {
        List<ClassSetup> shiftScoped = setupsForShift(classSetups, filterShift);
        HistoryFilterOptions.Snapshot snapshot = HistoryFilterOptions.from(
                shiftScoped, filterDepartmentId, filterSemesterId, filterSectionId);
        FilterOptions options = new FilterOptions();
        allSubjects = new ArrayList<>();
        if (classSetups != null) {
            for (ClassSetup setup : classSetups) {
                if (setup != null && setup.getSubject() != null) {
                    allSubjects.add(setup.getSubject());
                }
            }
        }
        // Department list respects shift so Morning/Evening are not mixed in the picker.
        HistoryFilterOptions.Snapshot departmentsSnapshot = HistoryFilterOptions.from(
                shiftScoped, null, null, null);
        copyOptions(departmentsSnapshot.departments, options.departments);
        copyOptions(snapshot.semesters, options.semesters);
        copyOptions(snapshot.sections, options.sections);
        copyOptions(snapshot.subjects, options.subjects);
        copyOptions(snapshot.courseCodes, options.courseCodes);
        boolean pruned = pruneStaleFilters(options);
        filterOptions = options;
        if (pruned) {
            List<ClassSetup> refreshedScoped = setupsForShift(classSetups, filterShift);
            HistoryFilterOptions.Snapshot refreshed = HistoryFilterOptions.from(
                    refreshedScoped, filterDepartmentId, filterSemesterId, filterSectionId);
            FilterOptions next = new FilterOptions();
            copyOptions(departmentsSnapshot.departments, next.departments);
            copyOptions(refreshed.semesters, next.semesters);
            copyOptions(refreshed.sections, next.sections);
            copyOptions(refreshed.subjects, next.subjects);
            copyOptions(refreshed.courseCodes, next.courseCodes);
            filterOptions = next;
        }
    }

    private static List<ClassSetup> setupsForShift(List<ClassSetup> setups, String shift) {
        if (setups == null) {
            return new ArrayList<>();
        }
        String normalized = HistoryFilter.normalizeShift(shift);
        if (normalized == null) {
            return setups;
        }
        List<ClassSetup> filtered = new ArrayList<>();
        for (ClassSetup setup : setups) {
            if (setup == null || setup.getDepartment() == null) {
                continue;
            }
            if (normalized.equals(com.example.studentattendance.model.DepartmentShift.normalize(
                    setup.getDepartment().getShift()))) {
                filtered.add(setup);
            }
        }
        return filtered;
    }

    private boolean pruneStaleFilters(FilterOptions options) {
        boolean pruned = false;
        if (filterDepartmentId != null && !containsOption(options.departments, filterDepartmentId)) {
            filterDepartmentId = null;
            pruned = true;
        }
        if (filterSemesterId != null && !containsOption(options.semesters, filterSemesterId)) {
            filterSemesterId = null;
            pruned = true;
        }
        if (filterSectionId != null && !containsOption(options.sections, filterSectionId)) {
            filterSectionId = null;
            pruned = true;
        }
        if (filterSubjectId != null && !containsOption(options.subjects, filterSubjectId)) {
            filterSubjectId = null;
            pruned = true;
        }
        if (filterCourseCode != null && !containsOption(options.courseCodes, filterCourseCode)) {
            filterCourseCode = null;
            pruned = true;
        }
        return pruned;
    }

    private static boolean containsOption(List<FilterOption> options, String id) {
        if (options == null || id == null) {
            return false;
        }
        for (FilterOption option : options) {
            if (id.equals(option.id)) {
                return true;
            }
        }
        return false;
    }

    private static void copyOptions(List<HistoryFilterOptions.Option> source, List<FilterOption> target) {
        if (source == null) {
            return;
        }
        for (HistoryFilterOptions.Option option : source) {
            target.add(new FilterOption(option.id, option.label));
        }
    }

    private String headerLabel(List<FilterOption> options, String id, String allLabel) {
        if (id == null) {
            return allLabel;
        }
        if (options != null) {
            for (FilterOption option : options) {
                if (id.equals(option.id)) {
                    return option.label;
                }
            }
        }
        return id;
    }

    private List<StudentReport> toStudentReports(List<SemesterReport.StudentRow> rows) {
        List<StudentReport> reports = new ArrayList<>();
        if (rows == null) {
            return reports;
        }
        for (SemesterReport.StudentRow row : rows) {
            reports.add(row.toStudentReport());
        }
        return reports;
    }

    private SemesterReport.SortMode toSemesterSort(SortMode mode) {
        if (mode == SortMode.NAME) {
            return SemesterReport.SortMode.NAME;
        }
        if (mode == SortMode.ROLL) {
            return SemesterReport.SortMode.ROLL;
        }
        if (mode == SortMode.PRESENT) {
            return SemesterReport.SortMode.PRESENT;
        }
        if (mode == SortMode.ABSENT) {
            return SemesterReport.SortMode.ABSENT;
        }
        return SemesterReport.SortMode.PERCENTAGE;
    }

    private SemesterReport.StatusFilter toSemesterStatus(StatusFilter filter) {
        if (filter == StatusFilter.BELOW_THRESHOLD) {
            return SemesterReport.StatusFilter.BELOW_THRESHOLD;
        }
        if (filter == StatusFilter.AT_OR_ABOVE) {
            return SemesterReport.StatusFilter.AT_OR_ABOVE;
        }
        return SemesterReport.StatusFilter.ALL;
    }

    private double currentThreshold() {
        User user = preferenceManager.getSavedUser();
        return user == null ? AttendancePolicy.DEFAULT_LOW_THRESHOLD : user.getLowAttendanceThreshold();
    }

    private Map<String, Integer> toDeltaMap(List<AttendanceAdjustment> data) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (data == null) {
            return map;
        }
        for (AttendanceAdjustment adj : data) {
            if (adj.getStudentId() == null) {
                continue;
            }
            Integer current = map.get(adj.getStudentId());
            map.put(adj.getStudentId(), (current == null ? 0 : current) + adj.getDelta());
        }
        return map;
    }

    private ReportAnalytics.SortMode toAnalyticsSort(SortMode mode) {
        if (mode == SortMode.PERCENTAGE) {
            return ReportAnalytics.SortMode.PERCENTAGE;
        }
        if (mode == SortMode.ROLL) {
            return ReportAnalytics.SortMode.ROLL;
        }
        return ReportAnalytics.SortMode.NAME;
    }

    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
