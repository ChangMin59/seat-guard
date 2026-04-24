package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.enrollment.Enrollment;
import com.example.seatguard.domain.enrollment.EnrollmentRepository;
import com.example.seatguard.domain.enrollment.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public Enrollment enroll(Long classId, Long userId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        // 1. 상태 체크
        if (clazz.getStatus() != ClassStatus.OPEN) {
            throw new RuntimeException("신청 불가 상태");
        }

        // 2. 정원 체크
        if (clazz.getCurrentCount() >= clazz.getCapacity()) {
            throw new RuntimeException("정원 초과");
        }

        // 3. 정원 증가 (핵심)
        clazz.increaseCount();

        // 4. Enrollment 생성
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .clazz(clazz)
                .status(EnrollmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment);
    }
}