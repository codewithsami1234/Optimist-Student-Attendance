package com.studentattendance.backend.config;

import com.studentattendance.backend.entity.*;
import com.studentattendance.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the same demo data the Phase 1 Android mocks used (departments,
 * semesters, sections, a semester-1 student roster, and the demo teacher
 * login) the first time the app starts against an empty database. This
 * keeps first-run behaviour identical to Phase 1 once the mock repositories
 * are replaced with real API calls. Safe to run on every boot: it checks
 * for existing rows first. Equivalent SQL lives in
 * database/02_seed_data.sql for environments that seed via plain SQL
 * instead of application boot.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedDemoUser;
    private final boolean seedAcademicData;

    public DataSeeder(DepartmentRepository departmentRepository, SemesterRepository semesterRepository,
                       SectionRepository sectionRepository, SubjectRepository subjectRepository,
                       StudentRepository studentRepository,
                       UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${app.seed.demo-user:true}") boolean seedDemoUser,
                       @Value("${app.seed.academic-data:true}") boolean seedAcademicData) {
        this.departmentRepository = departmentRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedDemoUser = seedDemoUser;
        this.seedAcademicData = seedAcademicData;
    }

    @Override
    public void run(String... args) {
        if (seedDemoUser) {
            insertDemoUser();
        }
        if (!seedAcademicData || departmentRepository.count() > 0) {
            return;
        }
        insertAcademicData();
    }

    private void insertDemoUser() {
        if (!userRepository.existsByEmailIgnoreCase("teacher@school.edu")) {
            User demo = new User("user_1", "Ayesha Khan", "teacher@school.edu",
                    passwordEncoder.encode("password123"), "TEACHER", "Assistant Professor");
            demo.setLowAttendanceThreshold(75.0);
            userRepository.save(demo);
        }
    }

    private void insertAcademicData() {
        List<Department> departments = new ArrayList<>();
        departments.add(new Department("dept_cs", "Computer Science", "CS", 0, "Dr. Ayesha Khan", "user_1"));
        departments.add(new Department("dept_ee", "Electrical Engineering", "EE", 0, "Dr. Bilal Ahmed", "user_1"));
        departments.add(new Department("dept_bba", "Business Administration", "BBA", 0, "Dr. Sana Malik", "user_1"));
        departments.add(new Department("dept_math", "Mathematics", "MATH", 0, "Dr. Imran Sheikh", "user_1"));
        departmentRepository.saveAll(departments);

        for (Department d : departments) {
            subjectRepository.save(new Subject("subj_gen_" + d.getId(), d.getId(), "General", "GEN"));
            subjectRepository.save(new Subject("subj_ds_" + d.getId(), d.getId(), "Data Structures", "DS"));
        }

        String[][] namesByDept = {
                {"Ahmed Raza", "Fatima Noor", "Hassan Ali", "Zainab Malik", "Usman Tariq",
                        "Ayesha Siddiqui", "Bilal Hussain", "Mariam Yousaf", "Omar Farooq", "Sana Iqbal"},
                {"Hamza Sheikh", "Iqra Batool", "Danish Khalid", "Rabia Anwar", "Talha Yaseen",
                        "Nida Kazmi", "Faisal Mehmood", "Sadia Rehman", "Adnan Qureshi", "Hira Aslam"},
                {"Waqas Ahmed", "Amna Riaz", "Shahzad Iqbal", "Komal Nawaz", "Junaid Akbar",
                        "Laiba Hassan", "Kashif Nadeem", "Mahnoor Zafar", "Salman Raza", "Anum Shahid"},
                {"Bilawal Khan", "Rida Fatima", "Zeeshan Haider", "Sidra Yousuf", "Arslan Butt",
                        "Noor ul Ain", "Kamran Ashraf", "Aleena Malik", "Rehan Zaman", "Warda Naseem"}
        };

        int rollCounter = 1;
        for (int di = 0; di < departments.size(); di++) {
            Department d = departments.get(di);
            for (int s = 1; s <= 4; s++) {
                String semId = d.getId() + "_sem" + s;
                semesterRepository.save(new Semester(semId, d.getId(), "Semester " + s, s));

                String[] sectionNames = {"Section A", "Section B"};
                for (String sn : sectionNames) {
                    String secId = semId + "_" + sn.replace(" ", "").toLowerCase();
                    Section section = new Section(secId, semId, sn, 0);
                    sectionRepository.save(section);

                    // Only semester 1 gets a full seeded roster, matching Phase 1 mock behaviour.
                    if (s == 1) {
                        String[] names = namesByDept[di % namesByDept.length];
                        int count = 0;
                        for (int i = 0; i < names.length; i++) {
                            String studentId = secId + "_st" + i;
                            String roll = d.getCode() + "-" + String.format("%03d", rollCounter++);
                            Student student = new Student(
                                    studentId, roll, names[i], d.getId(), semId, secId,
                                    names[i].toLowerCase().replace(" ", ".") + "@university.edu",
                                    "03" + (100000000 + (rollCounter * 7919) % 899999999));
                            studentRepository.save(student);
                            count++;
                        }
                        section.setStudentCount(count);
                        sectionRepository.save(section);
                    }
                }
            }
        }

        for (Department d : departments) {
            long total = studentRepository.findAll().stream()
                    .filter(st -> st.getDepartmentId().equals(d.getId())).count();
            d.setTotalStudents((int) total);
            departmentRepository.save(d);
        }
    }
}
