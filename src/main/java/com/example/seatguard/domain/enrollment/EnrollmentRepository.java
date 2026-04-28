package com.example.seatguard.domain.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.seatguard.domain.classs.Class;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // userId 기준 수강 신청 목록 조회
    List<Enrollment> findByUserId(Long userId);

    // 대기열에서 가장 먼저 신청한 1명 조회 (FIFO)
    Enrollment findFirstByClazzAndStatusOrderByCreatedAtAsc(
            Class clazz,
            EnrollmentStatus status
    );

    // 대기열 인원 수 조회
    long countByClazzAndStatus(Class clazz, EnrollmentStatus status);

    // 중복 신청 여부 확인
    boolean existsByClazzAndUserId(Class clazz, Long userId);
}
