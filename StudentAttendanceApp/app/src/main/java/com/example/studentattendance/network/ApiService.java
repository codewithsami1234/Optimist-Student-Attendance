package com.example.studentattendance.network;

import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.SectionStats;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.dto.AttendanceAdjustmentRequestDto;
import com.example.studentattendance.network.dto.AttendanceSaveRequestDto;
import com.example.studentattendance.network.dto.AuthResponseDto;
import com.example.studentattendance.network.dto.DepartmentCreateRequestDto;
import com.example.studentattendance.network.dto.DepartmentUpdateRequestDto;
import com.example.studentattendance.network.dto.GoogleAuthRequestDto;
import com.example.studentattendance.network.dto.LoginRequestDto;
import com.example.studentattendance.network.dto.ProfileUpdateRequestDto;
import com.example.studentattendance.network.dto.RegisterRequestDto;
import com.example.studentattendance.network.dto.SectionCreateRequestDto;
import com.example.studentattendance.network.dto.SectionUpdateRequestDto;
import com.example.studentattendance.network.dto.SemesterCreateRequestDto;
import com.example.studentattendance.network.dto.SemesterUpdateRequestDto;
import com.example.studentattendance.network.dto.StudentTransferRequestDto;
import com.example.studentattendance.network.dto.StudentUpdateRequestDto;
import com.example.studentattendance.network.dto.SubjectCreateRequestDto;
import com.example.studentattendance.network.dto.SubjectUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDto request);

    @POST("api/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @POST("api/auth/google")
    Call<AuthResponseDto> loginWithGoogle(@Body GoogleAuthRequestDto request);

    @GET("api/health")
    Call<java.util.Map<String, String>> health();

    @POST("api/auth/logout")
    Call<Void> logout();

    @GET("api/profile")
    Call<User> getProfile();

    @PUT("api/profile")
    Call<User> updateProfile(@Body ProfileUpdateRequestDto request);

    @GET("api/departments")
    Call<List<Department>> getDepartments();

    @POST("api/departments")
    Call<Department> createDepartment(@Body DepartmentCreateRequestDto request);

    @PUT("api/departments/{departmentId}")
    Call<Department> updateDepartment(@Path("departmentId") String departmentId,
                                      @Body DepartmentUpdateRequestDto request);

    @DELETE("api/departments/{departmentId}")
    Call<Void> deleteDepartment(@Path("departmentId") String departmentId);

    @GET("api/departments/{departmentId}/semesters")
    Call<List<Semester>> getSemesters(@Path("departmentId") String departmentId);

    @POST("api/departments/{departmentId}/semesters")
    Call<Semester> createSemester(@Path("departmentId") String departmentId, @Body SemesterCreateRequestDto request);

    @PUT("api/semesters/{semesterId}")
    Call<Semester> updateSemester(@Path("semesterId") String semesterId, @Body SemesterUpdateRequestDto request);

    @DELETE("api/semesters/{semesterId}")
    Call<Void> deleteSemester(@Path("semesterId") String semesterId);

    @GET("api/semesters/{semesterId}/sections")
    Call<List<Section>> getSections(@Path("semesterId") String semesterId);

    @POST("api/semesters/{semesterId}/sections")
    Call<Section> createSection(@Path("semesterId") String semesterId, @Body SectionCreateRequestDto request);

    @PUT("api/sections/{sectionId}")
    Call<Section> updateSection(@Path("sectionId") String sectionId, @Body SectionUpdateRequestDto request);

    @DELETE("api/sections/{sectionId}")
    Call<Void> deleteSection(@Path("sectionId") String sectionId);

    @GET("api/departments/{departmentId}/subjects")
    Call<List<Subject>> getSubjects(@Path("departmentId") String departmentId,
                                    @Query("includeArchived") boolean includeArchived);

    @POST("api/departments/{departmentId}/subjects")
    Call<Subject> createSubject(@Path("departmentId") String departmentId, @Body SubjectCreateRequestDto request);

    @PUT("api/subjects/{subjectId}")
    Call<Subject> updateSubject(@Path("subjectId") String subjectId, @Body SubjectUpdateRequestDto request);

    @DELETE("api/subjects/{subjectId}")
    Call<Void> deleteSubject(@Path("subjectId") String subjectId);

    @GET("api/students")
    Call<List<Student>> getStudents(@Query("departmentId") String departmentId,
                                     @Query("semesterId") String semesterId,
                                     @Query("sectionId") String sectionId,
                                     @Query("includeArchived") boolean includeArchived);

    @GET("api/students/all")
    Call<List<Student>> getAllStudents(@Query("includeArchived") boolean includeArchived);

    @POST("api/students")
    Call<Student> addStudent(@Body Student student);

    @PUT("api/students/{studentId}")
    Call<Student> updateStudent(@Path("studentId") String studentId, @Body StudentUpdateRequestDto request);

    @POST("api/students/{studentId}/transfer")
    Call<Student> transferStudent(@Path("studentId") String studentId, @Body StudentTransferRequestDto request);

    @DELETE("api/students/{studentId}")
    Call<Void> deleteStudent(@Path("studentId") String studentId);

    @POST("api/students/import")
    Call<ImportResult> importStudents(@Body List<Student> students);

    @GET("api/students/{studentId}/report")
    Call<StudentAttendanceReport> getStudentReport(@Path("studentId") String studentId);

    @POST("api/attendance")
    Call<AttendanceSession> saveAttendanceSession(@Body AttendanceSaveRequestDto request);

    @GET("api/attendance/session")
    Call<AttendanceSession> getAttendanceSession(@Query("departmentId") String departmentId,
                                                  @Query("semesterId") String semesterId,
                                                  @Query("sectionId") String sectionId,
                                                  @Query("date") String date,
                                                  @Query("subjectId") String subjectId);

    @GET("api/attendance/sessions")
    Call<List<AttendanceSession>> getAttendanceSessions(@Query("departmentId") String departmentId,
                                                         @Query("semesterId") String semesterId,
                                                         @Query("sectionId") String sectionId,
                                                         @Query("subjectId") String subjectId,
                                                         @Query("fromDate") String fromDate,
                                                         @Query("toDate") String toDate,
                                                         @Query("page") Integer page,
                                                         @Query("size") Integer size);

    @GET("api/reports/sessions")
    Call<List<AttendanceSession>> getSessionsForReports();

    @GET("api/dashboard/stats")
    Call<DashboardStats> getDashboardStats();

    @GET("api/analytics/section-stats")
    Call<List<SectionStats>> getSectionStats(@Query("departmentId") String departmentId,
                                              @Query("semesterId") String semesterId,
                                              @Query("subjectId") String subjectId);

    @GET("api/analytics/trend")
    Call<List<DailyTrend>> getTrend(@Query("departmentId") String departmentId,
                                     @Query("subjectId") String subjectId,
                                     @Query("days") int days);

    @GET("api/analytics/low-attendance")
    Call<List<StudentAttendanceSummary>> getLowAttendance(@Query("threshold") Double threshold,
                                                           @Query("limit") int limit);

    @POST("api/students/{studentId}/adjustments")
    Call<AttendanceAdjustment> createAdjustment(@Path("studentId") String studentId,
                                                 @Body AttendanceAdjustmentRequestDto request);

    @GET("api/students/{studentId}/adjustments")
    Call<List<AttendanceAdjustment>> getAdjustmentHistory(@Path("studentId") String studentId);

    @GET("api/notifications")
    Call<List<Notification>> getNotifications(@Query("page") Integer page, @Query("size") Integer size);

    @GET("api/notifications/unread-count")
    Call<Map<String, Long>> getUnreadNotificationCount();

    @PUT("api/notifications/{notificationId}/read")
    Call<Void> markNotificationRead(@Path("notificationId") String notificationId);

    @PUT("api/notifications/read-all")
    Call<Void> markAllNotificationsRead();
}
