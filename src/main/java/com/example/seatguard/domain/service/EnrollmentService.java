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
import com.example.seatguard.domain.dto.EnrollmentResponseDto;
import java.util.List;

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

        // 3. 정원 증가
        clazz.increaseCount();

        // 4. 수강 신청 생성
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .clazz(clazz)
                .status(EnrollmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment);
    }

    // 수강 신청 취소 처리
    @Transactional
    public Enrollment cancel(Long enrollmentId) {

        // 1. 수강 신청 조회 (없으면 예외)
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("신청 없음"));

        // 2. 이미 취소된 상태인지 확인 (중복 취소 방지)
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new RuntimeException("이미 취소됨");
        }

        // 3. 상태를 CANCELLED로 변경
        enrollment.changeStatus(EnrollmentStatus.CANCELLED);

        // 4. 해당 강의의 현재 인원 감소
        enrollment.getClazz().decreaseCount();

        // 5. 변경된 enrollment 반환 (JPA가 자동 반영)
        return enrollment;
    }

    // 내 수강 신청 목록 조회
    public List<EnrollmentResponseDto> getMyEnrollments(Long userId) {

        return enrollmentRepository.findByUserId(userId)
                .stream()
                .map(EnrollmentResponseDto::new)
                .toList();
    }

    // 수강 신청 결제 완료 처리
    @Transactional
    public Enrollment confirm(Long enrollmentId) {

        // 1. 수강 신청 조회
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("신청 없음"));

        // 2. 이미 확정된 경우 방지
        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            throw new RuntimeException("이미 확정됨");
        }

        // 3. 취소된 건 결제 불가
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new RuntimeException("취소된 신청");
        }

        // 4. 상태 변경
        enrollment.changeStatus(EnrollmentStatus.CONFIRMED);

        return enrollment;
    }
}