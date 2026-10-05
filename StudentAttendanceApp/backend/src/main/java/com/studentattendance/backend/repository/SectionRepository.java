package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, String> {
    List<Section> findBySemesterIdOrderByNameAsc(String semesterId);
    boolean existsBySemesterIdAndNameIgnoreCase(String semesterId, String name);
    long countBySemesterId(String semesterId);
}
