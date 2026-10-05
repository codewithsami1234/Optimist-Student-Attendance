package com.studentattendance.backend.service;

import com.studentattendance.backend.domain.AttendancePolicy;
import com.studentattendance.backend.dto.DailyTrendDto;
import com.studentattendance.backend.dto.SectionStatsDto;
import com.studentattendance.backend.dto.StudentAttendanceReportDto;
import com.studentattendance.backend.dto.StudentAttendanceSummaryDto;
import com.studentattendance.backend.entity.AttendanceRecord;
import com.studentattendance.backend.entity.AttendanceStatus;
import com.studentattendance.backend.entity.Section;
import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.repository.AttendanceRecordRepository;
import com.studentattendance.backend.repository.DepartmentRepository;
import com.studentattendance.backend.repository.SectionRepository;
import com.studentattendance.backend.repository.SemesterRepository;
import com.studentattendance.backend.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AttendanceRecordRepository recordRepository;
    private final SectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;
    private final StudentService studentService;
    private final AttendanceAdjustmentService adjustmentService;
    private final AuthContext authContext;

    public AnalyticsService(AttendanceRecordRepository recordRepository,
                             SectionRepository sectionRepository,
                             SemesterRepository semesterRepository,
                             DepartmentRepository departmentRepository,
                             DepartmentService departmentService,
                             StudentService studentService,
                             AttendanceAdjustmentService adjustmentService,
                             AuthContext authContext) {
        this.recordRepository = recordRepository;
        this.sectionRepository = sectionRepository;
        this.semesterRepository = semesterRepository;
        this.departmentRepository = departmentRepository;
        this.departmentService = departmentService;
        this.studentService = studentService;
        this.adjustmentService = adjustmentService;
        this.authContext = authContext;
    }

    public double currentThreshold() {
        return AttendancePolicy.sanitizeThreshold(authContext.currentUser().getLowAttendanceThreshold());
    }

    public List<SectionStatsDto> getSectionStats(String departmentId, String semesterId, String subjectId) {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        if (blankToNull(departmentId) != null) {
            departmentService.requireOwnedDepartment(departmentId);
        }
        List<AttendanceRecord> records = recordRepository.search(
                ownedDepartmentIds, blankToNull(departmentId), blankToNull(semesterId), null, blankToNull(subjectId));

        Map<String, int[]> totalsBySection = new LinkedHashMap<>(); // [present, absent, late, leave]
        Map<String, AttendanceRecord> sampleBySection = new LinkedHashMap<>();
        for (AttendanceRecord r : records) {
            int[] totals = totalsBySection.computeIfAbsent(r.getSectionId(), k -> new int[4]);
            bump(totals, r.getStatus());
            sampleBySection.putIfAbsent(r.getSectionId(), r);
        }

        List<SectionStatsDto> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totalsBySection.entrySet()) {
            String sectionId = entry.getKey();
            AttendanceRecord sample = sampleBySection.get(sectionId);
            int[] t = entry.getValue();
            int totalStudents = sectionRepository.findById(sectionId).map(Section::getStudentCount).orElse(0);
            int presentLike = t[0] + t[2];
            int totalMarked = t[0] + t[1] + t[2] + t[3];
            SectionStatsDto dto = new SectionStatsDto(
                    sample.getDepartmentId(), null, sample.getSemesterId(), null,
                    sectionId, null, totalStudents, presentLike, totalMarked);
            dto.setAttendanceRate(AttendancePolicy.rate(t[0], t[1], t[2], t[3], 0));
            result.add(dto);
        }
        for (SectionStatsDto dto : result) {
            sectionRepository.findById(dto.getSectionId()).ifPresent(section -> dto.setSectionName(section.getName()));
            semesterRepository.findById(dto.getSemesterId()).ifPresent(sem -> dto.setSemesterName(sem.getName()));
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(dep -> dto.setDepartmentName(dep.getName()));
        }
        result.sort(Comparator.comparingDouble(SectionStatsDto::getAttendanceRate));
        return result;
    }

    public List<DailyTrendDto> getTrend(String departmentId, String subjectId, int days) {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        int safeDays = Math.max(1, Math.min(days, 90));
        List<DailyTrendDto> trend = new ArrayList<>();
        Map<String, int[]> totalsByDate = new LinkedHashMap<>();
        for (int i = safeDays - 1; i >= 0; i--) {
            totalsByDate.put(LocalDate.now().minusDays(i).toString(), new int[4]);
        }

        if (!ownedDepartmentIds.isEmpty()) {
            if (blankToNull(departmentId) != null) {
                departmentService.requireOwnedDepartment(departmentId);
            }
            String fromDate = LocalDate.now().minusDays(safeDays - 1L).toString();
            List<AttendanceRecord> records = recordRepository.findSince(
                    ownedDepartmentIds, fromDate, blankToNull(departmentId), blankToNull(subjectId));
            for (AttendanceRecord r : records) {
                int[] totals = totalsByDate.get(r.getDate());
                if (totals == null) {
                    continue;
                }
                bump(totals, r.getStatus());
            }
        }

        for (Map.Entry<String, int[]> entry : totalsByDate.entrySet()) {
            int[] t = entry.getValue();
            DailyTrendDto dto = new DailyTrendDto(entry.getKey(), t[0], t[1], t[2], t[3]);
            dto.setAttendanceRate(AttendancePolicy.rate(t[0], t[1], t[2], t[3], 0));
            trend.add(dto);
        }
        return trend;
    }

    public List<StudentAttendanceSummaryDto> getLowAttendance(Double threshold, int limit) {
        double cut = AttendancePolicy.sanitizeThreshold(
                threshold != null ? threshold : currentThreshold());
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        List<AttendanceRecord> records = recordRepository.findByDepartmentIdIn(ownedDepartmentIds);
        Map<String, int[]> totalsByStudent = new LinkedHashMap<>();
        Map<String, AttendanceRecord> sampleByStudent = new LinkedHashMap<>();
        for (AttendanceRecord r : records) {
            int[] totals = totalsByStudent.computeIfAbsent(r.getStudentId(), k -> new int[4]);
            bump(totals, r.getStatus());
            sampleByStudent.putIfAbsent(r.getStudentId(), r);
        }

        List<StudentAttendanceSummaryDto> summaries = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totalsByStudent.entrySet()) {
            int[] t = entry.getValue();
            int countable = t[0] + t[1] + t[2];
            if (countable == 0) {
                continue;
            }
            int adjustmentDelta = adjustmentService.getNetDelta(entry.getKey());
            double rate = AttendancePolicy.rate(t[0], t[1], t[2], t[3], adjustmentDelta);
            if (rate > cut) {
                continue;
            }
            AttendanceRecord sample = sampleByStudent.get(entry.getKey());
            String sectionName = sectionRepository.findById(sample.getSectionId()).map(Section::getName).orElse(null);
            int presentLike = t[0] + t[2] + adjustmentDelta;
            StudentAttendanceSummaryDto dto = new StudentAttendanceSummaryDto(
                    entry.getKey(), sample.getStudentName(), sample.getRollNumber(),
                    sample.getSectionId(), sectionName, Math.max(0, presentLike), countable);
            dto.setAttendanceRate(rate);
            summaries.add(dto);
        }
        summaries.sort(Comparator.comparingDouble(StudentAttendanceSummaryDto::getAttendanceRate));
        return summaries.size() > limit ? summaries.subList(0, limit) : summaries;
    }

    public StudentAttendanceReportDto getStudentReport(String studentId) {
        Student student = studentService.getOwnedStudentOrThrow(studentId);
        List<AttendanceRecord> records = recordRepository.findByStudentIdOrderByDateDesc(studentId);

        StudentAttendanceReportDto dto = new StudentAttendanceReportDto();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setRollNumber(student.getRollNumber());
        dto.setDepartmentId(student.getDepartmentId());
        dto.setSemesterId(student.getSemesterId());
        dto.setSectionId(student.getSectionId());
        sectionRepository.findById(student.getSectionId()).ifPresent(s -> dto.setSectionName(s.getName()));
        semesterRepository.findById(student.getSemesterId()).ifPresent(s -> dto.setSemesterName(s.getName()));
        departmentRepository.findById(student.getDepartmentId()).ifPresent(d -> dto.setDepartmentName(d.getName()));

        int present = 0, absent = 0, late = 0, leave = 0;
        for (AttendanceRecord r : records) {
            switch (r.getStatus()) {
                case PRESENT: present++; break;
                case ABSENT: absent++; break;
                case LATE: late++; break;
                case LEAVE: leave++; break;
                default: break;
            }
        }
        int totalMarked = present + absent + late + leave;
        int adjustmentDelta = adjustmentService.getNetDelta(studentId);
        int adjustedPresent = present + late + adjustmentDelta;

        dto.setPresentCount(present);
        dto.setAbsentCount(absent);
        dto.setLateCount(late);
        dto.setLeaveCount(leave);
        dto.setTotalMarked(totalMarked);
        dto.setAttendanceRate(AttendancePolicy.rate(present, absent, late, leave, adjustmentDelta));
        dto.setAdjustmentDelta(adjustmentDelta);
        dto.setAdjustedPresentCount(Math.max(0, adjustedPresent));
        dto.setRecords(records);
        return dto;
    }

    private void bump(int[] totals, AttendanceStatus status) {
        switch (status) {
            case PRESENT: totals[0]++; break;
            case ABSENT: totals[1]++; break;
            case LATE: totals[2]++; break;
            case LEAVE: totals[3]++; break;
            default: break;
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public static void requireValidDate(String date, String fieldLabel) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new BadRequestException(fieldLabel + " must be a valid date (yyyy-MM-dd).");
        }
    }
}
