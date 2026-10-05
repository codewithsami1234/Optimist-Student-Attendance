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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwnershipAndImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void otherTeacherCannotReadSomeoneElsesDepartment() throws Exception {
        String tokenA = login("teacher@school.edu", "password123");

        MvcResult deptResult = mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode departments = objectMapper.readTree(deptResult.getResponse().getContentAsString());
        String departmentId = departments.get(0).get("id").asText();

        String unique = "other_" + System.currentTimeMillis() + "@school.edu";
        String registerBody = "{\"name\":\"Other Teacher\",\"email\":\"" + unique
                + "\",\"password\":\"password123\",\"designation\":\"Lecturer\"}";
        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenB = objectMapper.readTree(register.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/departments/" + departmentId + "/semesters")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void csvImportReportsInvalidAndDuplicateRowsInsteadOfSilentSkip() throws Exception {
        String token = login("teacher@school.edu", "password123");
        JsonNode section = firstSection(token);
        String sectionId = section.get("id").asText();
        String departmentId = section.get("departmentId") != null
                ? section.get("departmentId").asText() : null;

        MvcResult studentsResult = mockMvc.perform(get("/api/students")
                        .param("sectionId", sectionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode students = objectMapper.readTree(studentsResult.getResponse().getContentAsString());
        String existingRoll = students.get(0).get("rollNumber").asText();
        if (departmentId == null) {
            departmentId = students.get(0).get("departmentId").asText();
        }
        String semesterId = students.get(0).get("semesterId").asText();

        String body = "["
                + "{\"name\":\"\",\"rollNumber\":\"X-1\",\"sectionId\":\"" + sectionId
                + "\",\"departmentId\":\"" + departmentId + "\",\"semesterId\":\"" + semesterId + "\"},"
                + "{\"name\":\"Duplicate Person\",\"rollNumber\":\"" + existingRoll + "\",\"sectionId\":\"" + sectionId
                + "\",\"departmentId\":\"" + departmentId + "\",\"semesterId\":\"" + semesterId + "\"},"
                + "{\"name\":\"Fresh Student\",\"rollNumber\":\"IMP-" + System.currentTimeMillis()
                + "\",\"sectionId\":\"" + sectionId + "\",\"departmentId\":\"" + departmentId
                + "\",\"semesterId\":\"" + semesterId + "\"}"
                + "]";

        MvcResult importResult = mockMvc.perform(post("/api/students/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode result = objectMapper.readTree(importResult.getResponse().getContentAsString());
        assertEquals(1, result.get("imported").asInt());
        assertTrue(result.get("invalid").asInt() >= 1);
        assertTrue(result.get("duplicates").asInt() >= 1);
        assertTrue(result.get("errors").isArray() && result.get("errors").size() >= 2);
    }

    private String login(String email, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode firstSection(String token) throws Exception {
        JsonNode departments = objectMapper.readTree(mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        String departmentId = departments.get(0).get("id").asText();
        JsonNode semesters = objectMapper.readTree(mockMvc.perform(get("/api/departments/" + departmentId + "/semesters")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        String semesterId = semesters.get(0).get("id").asText();
        JsonNode sections = objectMapper.readTree(mockMvc.perform(get("/api/semesters/" + semesterId + "/sections")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        return sections.get(0);
    }
}
