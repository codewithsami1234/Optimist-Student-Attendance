package com.studentattendance.backend.service;

import com.studentattendance.backend.dto.AttendanceRecordInput;
import com.studentattendance.backend.dto.AttendanceSaveRequest;
import com.studentattendance.backend.entity.AttendanceRecord;
import com.studentattendance.backend.entity.AttendanceSession;
import com.studentattendance.backend.entity.AttendanceStatus;
import com.studentattendance.backend.entity.Section;
import com.studentattendance.backend.entity.Semester;
import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.entity.Subject;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.repository.AttendanceSessionRepository;
import com.studentattendance.backend.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AttendanceSessionRepository sessionRepository;
    private final DepartmentService departmentService;
    private final SubjectService subjectService;
    private final AnalyticsService analyticsService;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceSessionRepository sessionRepository, DepartmentService departmentService,
                              SubjectService subjectService, AnalyticsService analyticsService,
                              NotificationService notificationService, StudentRepository studentRepository) {
        this.sessionRepository = sessionRepository;
        this.departmentService = departmentService;
        this.subjectService = subjectService;
        this.analyticsService = analyticsService;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public AttendanceSession saveSession(AttendanceSaveRequest request, String markedByName) {
        if (request.getSectionId() == null || request.getSectionId().isBlank()) {
            throw new BadRequestException("Please select a section first.");
        }
        departmentService.requireOwnedSection(request.getSectionId());
        Section section = departmentService.getSectionOrThrow(request.getSectionId());
        Semester semester = departmentService.getSemesterOrThrow(section.getSemesterId());

        Subject subject = resolveSubject(request.getSubjectId(), semester.getDepartmentId());

        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new BadRequestException("There are no students to save attendance for.");
        }
        for (AttendanceRecordInput r : request.getRecords()) {
            if (r.getStatus() == null || r.getStatus() == AttendanceStatus.NOT_MARKED) {
                throw new BadRequestException("Please mark attendance for every student before saving.");
            }
        }
        AnalyticsService.requireValidDate(request.getDate(), "date");
        if (LocalDate.parse(request.getDate()).isAfter(LocalDate.now())) {
            throw new BadRequestException("Attendance cannot be marked for a future date.");
        }

        List<Student> roster = studentRepository.findBySectionIdAndArchivedFalseOrderByRollNumberAsc(request.getSectionId());
        if (roster.isEmpty()) {
            throw new BadRequestException("There are no students to save attendance for.");
        }
        Map<String, Student> rosterById = roster.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
        Set<String> submittedIds = new HashSet<>();
        for (AttendanceRecordInput input : request.getRecords()) {
            if (input.getStudentId() == null || !rosterById.containsKey(input.getStudentId())) {
                throw new BadRequestException("One or more students are not in this section.");
            }
            if (!submittedIds.add(input.getStudentId())) {
                throw new BadRequestException("Duplicate student in this attendance session.");
            }
        }
        if (submittedIds.size() != roster.size()) {
            throw new BadRequestException("Please mark attendance for every student before saving.");
        }

        String sessionId = request.getSectionId() + "_" + subject.getId() + "_" + request.getDate();

        sessionRepository.findBySectionIdAndSubjectIdAndDate(
                request.getSectionId(), subject.getId(), request.getDate())
                .ifPresent(existing -> {
                    sessionRepository.delete(existing);
                    sessionRepository.flush();
                });
        sessionRepository.findById(sessionId).ifPresent(existing -> {
            sessionRepository.delete(existing);
            sessionRepository.flush();
        });

        AttendanceSession session = new AttendanceSession();
        session.setId(sessionId);
        session.setDepartmentId(semester.getDepartmentId());
        session.setDepartmentName(request.getDepartmentName() != null
                ? request.getDepartmentName() : departmentService.requireOwnedDepartment(semester.getDepartmentId()).getName());
        session.setSemesterId(semester.getId());
        session.setSemesterName(request.getSemesterName() != null ? request.getSemesterName() : semester.getName());
        session.setSectionId(section.getId());
        session.setSectionName(request.getSectionName() != null ? request.getSectionName() : section.getName());
        session.setSubjectId(subject.getId());
        session.setSubjectName(subject.getName());
        session.setDate(request.getDate());
        session.setCreatedAtMillis(System.currentTimeMillis());

        for (AttendanceRecordInput input : request.getRecords()) {
            Student student = rosterById.get(input.getStudentId());
            AttendanceRecord record = new AttendanceRecord();
            record.setId(sessionId + "_" + student.getId());
            record.setSession(session);
            record.setStudentId(student.getId());
            record.setStudentName(student.getName());
            record.setRollNumber(student.getRollNumber());
            record.setDepartmentId(semester.getDepartmentId());
            record.setSemesterId(semester.getId());
            record.setSectionId(section.getId());
            record.setSubjectId(subject.getId());
            record.setDate(request.getDate());
            record.setStatus(input.getStatus());
            record.setMarkedBy(markedByName);
            record.setMarkedAtMillis(System.currentTimeMillis());
            session.getRecords().add(record);
        }

        AttendanceSession saved = sessionRepository.save(session);
        notifyLowAttendanceForAbsentees(saved);
        return saved;
    }

    private Subject resolveSubject(String subjectId, String departmentId) {
        if (subjectId == null || subjectId.isBlank()) {
            return departmentService.ensureGeneralSubject(departmentId);
        }
        Subject subject = subjectService.requireOwned(subjectId);
        if (!departmentId.equals(subject.getDepartmentId())) {
            throw new BadRequestException("That subject does not belong to this department.");
        }
        if (subject.isArchived()) {
            throw new BadRequestException("That subject has been archived.");
        }
        return subject;
    }

    private void notifyLowAttendanceForAbsentees(AttendanceSession session) {
        long oneDayAgo = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
        String teacherId = departmentService.requireOwnedDepartment(session.getDepartmentId()).getOwnerId();
        double threshold = analyticsService.currentThreshold();
        for (AttendanceRecord record : session.getRecords()) {
            if (record.getStatus() != AttendanceStatus.ABSENT && record.getStatus() != AttendanceStatus.LATE) {
                continue;
            }
            var report = analyticsService.getStudentReport(record.getStudentId());
            if (report.getAttendanceRate() <= threshold) {
                notificationService.notifyIfNotDuplicate(teacherId, "LOW_ATTENDANCE",
                        String.format("%s's attendance has dropped to %.0f%%.",
                                report.getStudentName(), report.getAttendanceRate()),
                        record.getStudentId(), oneDayAgo);
            }
        }
    }

    @Transactional(readOnly = true)
    public AttendanceSession getSession(String departmentId, String semesterId, String sectionId,
                                         String subjectId, String date) {
        departmentService.requireOwnedSection(sectionId);
        String resolvedSubjectId = subjectId;
        if (resolvedSubjectId == null || resolvedSubjectId.isBlank()) {
            Section section = departmentService.getSectionOrThrow(sectionId);
            Semester semester = departmentService.getSemesterOrThrow(section.getSemesterId());
            resolvedSubjectId = departmentService.ensureGeneralSubject(semester.getDepartmentId()).getId();
        } else {
            departmentService.requireOwnedSubject(resolvedSubjectId);
        }
        Optional<AttendanceSession> byTriple = sessionRepository
                .findBySectionIdAndSubjectIdAndDate(sectionId, resolvedSubjectId, date);
        if (byTriple.isPresent()) {
            return byTriple.get();
        }
        if (departmentId != null && semesterId != null) {
            return sessionRepository
                    .findByDepartmentIdAndSemesterIdAndSectionIdAndDate(departmentId, semesterId, sectionId, date)
                    .orElse(null);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<AttendanceSession> getSessions(String departmentId, String semesterId, String sectionId,
                                                String subjectId, String fromDate, String toDate) {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        String dept = blankToNull(departmentId);
        if (dept != null) {
            departmentService.requireOwnedDepartment(dept);
        }
        String from = blankToNull(fromDate);
        String to = blankToNull(toDate);
        if (from != null) {
            AnalyticsService.requireValidDate(from, "fromDate");
        }
        if (to != null) {
            AnalyticsService.requireValidDate(to, "toDate");
        }
        return sessionRepository.search(ownedDepartmentIds, dept, blankToNull(semesterId),
                blankToNull(sectionId), blankToNull(subjectId), from, to);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSession> getSessionsPaged(String departmentId, String semesterId, String sectionId,
                                                     String subjectId, String fromDate, String toDate,
                                                     Integer page, Integer size) {
        if (page == null || size == null) {
            List<AttendanceSession> all = getSessions(departmentId, semesterId, sectionId, subjectId, fromDate, toDate);
            if (all.size() > 500) {
                return all.subList(0, 500);
            }
            return all;
        }
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        String dept = blankToNull(departmentId);
        if (dept != null) {
            departmentService.requireOwnedDepartment(dept);
        }
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        int safePage = Math.max(0, page);
        return sessionRepository.searchPage(ownedDepartmentIds, dept, blankToNull(semesterId),
                blankToNull(sectionId), blankToNull(subjectId), blankToNull(fromDate), blankToNull(toDate),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "date")
                        .and(Sort.by(Sort.Direction.DESC, "createdAtMillis")))).getContent();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    @Transactional(readOnly = true)
    public List<AttendanceSession> getSessionsForReports() {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        return sessionRepository.findByDepartmentIdInOrderByDateDescCreatedAtMillisDesc(ownedDepartmentIds);
    }
}
