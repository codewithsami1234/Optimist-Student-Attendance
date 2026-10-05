package com.studentattendance.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test for the exact flow the Android app drives:
 * login -> list departments/semesters/sections -> list students -> save an
 * attendance session -> read it back via session/sessions/reports.
 * <p>
 * This is also a regression guard for the AttendanceSession <-> AttendanceRecord
 * bidirectional JPA mapping: before {@code @JsonIgnore} was added to
 * {@link com.studentattendance.backend.entity.AttendanceRecord#getSession()},
 * every one of the assertions below that touches a session's JSON body would
 * fail with a StackOverflowError instead of a normal response, because
 * Jackson would recurse forever serializing session -> records -> record's
 * session -> records -> ...
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginThenSaveAndReadBackAttendance_endToEnd() throws Exception {
        // 1. Login with the seeded demo teacher account.
        String loginBody = "{\"email\":\"teacher@school.edu\",\"password\":\"password123\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();
        assertFalse(token.isBlank(), "login should return a non-empty JWT");
        assertEquals("Ayesha Khan", loginJson.get("user").get("name").asText());

        // 2. A protected endpoint without a token is rejected.
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());

        // 3. Walk Department -> Semester -> Section -> Students, same as SelectionActivity.
        MvcResult deptResult = mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode departments = objectMapper.readTree(deptResult.getResponse().getContentAsString());
        assertTrue(departments.isArray() && departments.size() > 0);
        String departmentId = departments.get(0).get("id").asText();
        String departmentName = departments.get(0).get("name").asText();

        MvcResult semResult = mockMvc.perform(get("/api/departments/" + departmentId + "/semesters")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode semesters = objectMapper.readTree(semResult.getResponse().getContentAsString());
        String semesterId = semesters.get(0).get("id").asText();
        String semesterName = semesters.get(0).get("name").asText();

        MvcResult secResult = mockMvc.perform(get("/api/semesters/" + semesterId + "/sections")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sections = objectMapper.readTree(secResult.getResponse().getContentAsString());
        String sectionId = sections.get(0).get("id").asText();
        String sectionName = sections.get(0).get("name").asText();

        MvcResult studentsResult = mockMvc.perform(get("/api/students")
                        .param("departmentId", departmentId)
                        .param("semesterId", semesterId)
                        .param("sectionId", sectionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode students = objectMapper.readTree(studentsResult.getResponse().getContentAsString());
        assertTrue(students.isArray() && students.size() > 0, "seeded section should have a roster");

        // 4. Mark attendance for every student (Present) and save, as MarkAttendanceActivity does.
        StringBuilder records = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            JsonNode s = students.get(i);
            if (i > 0) records.append(",");
            records.append("{\"studentId\":\"").append(s.get("id").asText())
                    .append("\",\"studentName\":\"").append(s.get("name").asText())
                    .append("\",\"rollNumber\":\"").append(s.get("rollNumber").asText())
                    .append("\",\"status\":\"PRESENT\"}");
        }
        records.append("]");
        String date = java.time.LocalDate.now().minusDays(1).toString();
        String saveBody = "{"
                + "\"departmentId\":\"" + departmentId + "\","
                + "\"departmentName\":\"" + departmentName + "\","
                + "\"semesterId\":\"" + semesterId + "\","
                + "\"semesterName\":\"" + semesterName + "\","
                + "\"sectionId\":\"" + sectionId + "\","
                + "\"sectionName\":\"" + sectionName + "\","
                + "\"date\":\"" + date + "\","
                + "\"records\":" + records
                + "}";

        MvcResult saveResult = mockMvc.perform(post("/api/attendance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode saved = objectMapper.readTree(saveResult.getResponse().getContentAsString());
        assertEquals(students.size(), saved.get("presentCount").asInt());
        // The record objects must NOT contain a "session" property (JsonIgnore regression guard).
        assertFalse(saved.get("records").get(0).has("session"));

        // 5. Read the same session back (History / Mark Attendance re-open), and the sessions list / reports feed.
        mockMvc.perform(get("/api/attendance/session")
                        .param("departmentId", departmentId)
                        .param("semesterId", semesterId)
                        .param("sectionId", sectionId)
                        .param("date", date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attendance/sessions")
                        .param("departmentId", departmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/sessions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 6. Phase 3 analytics endpoints, all fed by the session just saved.
        MvcResult statsResult = mockMvc.perform(get("/api/analytics/section-stats")
                        .param("departmentId", departmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sectionStats = objectMapper.readTree(statsResult.getResponse().getContentAsString());
        assertTrue(sectionStats.isArray() && sectionStats.size() > 0);
        assertEquals(100.0, sectionStats.get(0).get("attendanceRate").asDouble(), 0.001);

        MvcResult trendResult = mockMvc.perform(get("/api/analytics/trend")
                        .param("departmentId", departmentId)
                        .param("days", "7")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode trend = objectMapper.readTree(trendResult.getResponse().getContentAsString());
        assertEquals(7, trend.size());
        boolean matchedSavedDay = false;
        for (JsonNode day : trend) {
            if (date.equals(day.get("date").asText())) {
                assertEquals(students.size(), day.get("presentCount").asInt());
                matchedSavedDay = true;
            }
        }
        assertTrue(matchedSavedDay, "trend should include the saved session date");

        mockMvc.perform(get("/api/analytics/low-attendance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 7. Per-student attendance report reflects the same save.
        String firstStudentId = students.get(0).get("id").asText();
        MvcResult reportResult = mockMvc.perform(get("/api/students/" + firstStudentId + "/report")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode report = objectMapper.readTree(reportResult.getResponse().getContentAsString());
        assertEquals(1, report.get("presentCount").asInt());
        assertEquals(100.0, report.get("attendanceRate").asDouble(), 0.001);
        assertEquals(1, report.get("records").size());

        // 8. Business-rule validation: future-dated attendance is rejected with 400, not a 500/stack trace.
        String futureBody = saveBody.replace(date, "2099-01-01");
        mockMvc.perform(post("/api/attendance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(futureBody))
                .andExpect(status().isBadRequest());
    }
}
