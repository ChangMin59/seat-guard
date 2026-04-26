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

    // 수강 신청 처리
    @Transactional
    public Enrollment enroll(Long classId, Long userId) {

        // 강의 조회
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        // 현재 시간 기준으로 상태 최신화
        clazz.updateStatusByTime(LocalDateTime.now());

        // 정원 먼저 체크
        if (clazz.getCurrentCount() >= clazz.getCapacity()) {
            throw new RuntimeException("정원 초과");
        }
        // 상태 체크
        if (clazz.getStatus() != ClassStatus.OPEN) {
            throw new RuntimeException("신청 불가 상태");
        }

        try {
            // 정원 증가 (정원 초과 + 동시성 처리)
            clazz.increaseCount();

        } catch (Exception e) {
            // 정원 초과 또는 동시 요청 충돌
            throw new RuntimeException("정원 초과 또는 동시 요청 충돌");
        }

        // 수강 신청 생성
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

        // 수강 신청 조회
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("신청 없음"));

        // 이미 취소된 경우
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new RuntimeException("이미 취소됨");
        }

        // 결제 완료 후 7일 제한
        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            if (enrollment.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
                throw new RuntimeException("결제 후 7일이 지나 취소 불가");
            }
        }

        // 강의 시작 이후 취소 불가
        if (LocalDateTime.now().isAfter(enrollment.getClazz().getStartDate())) {
            throw new RuntimeException("강의 시작 후 취소 불가");
        }

        // 상태 변경
        enrollment.changeStatus(EnrollmentStatus.CANCELLED);

        // 정원 감소
        enrollment.getClazz().decreaseCount();

        // 정원 변경 이후 상태 재계산 (CLOSED → OPEN 복구 반영)
        enrollment.getClazz().updateStatusByTime(LocalDateTime.now());

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