package com.manogna.recruitment.repository;

import com.manogna.recruitment.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentId(Long studentId);
    List<Application> findByJobId(Long jobId);
    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);
}
