package com.example.seatguard.domain.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // userId 기준 수강 신청 목록 조회
    List<Enrollment> findByUserId(Long userId);
}
