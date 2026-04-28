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
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 수강 신청 처리 (대기열)
    @Transactional
    public Enrollment enroll(Long classId, Long userId) {

        // 강의 조회
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        // 현재 시간 기준으로 상태 최신화
        clazz.updateStatusByTime(LocalDateTime.now());

        // DRAFT 상태만 신청 불가 (CLOSED는 WAITING 허용)
        if (clazz.getStatus() == ClassStatus.DRAFT) {
            throw new RuntimeException("신청 불가 상태");
        }

        // 중복 신청 체크
        boolean exists = enrollmentRepository
                .existsByClazzAndUserId(clazz, userId);

        if (exists) {
            throw new RuntimeException("이미 신청한 강의입니다.");
        }

        //5. 정원에 따라 분기
        EnrollmentStatus status;

        if (clazz.getCurrentCount() >= clazz.getCapacity()) {
            // 정원 초과 → 대기열
            status = EnrollmentStatus.WAITING;
        } else {
            // 자리 있음 → 신청
            status = EnrollmentStatus.PENDING;
            clazz.increaseCount();
        }

        // 수강 신청 생성
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .clazz(clazz)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            return enrollmentRepository.save(enrollment);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("이미 신청한 강의입니다.");
        }
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

        // 이전 상태 저장
        EnrollmentStatus prevStatus = enrollment.getStatus();

        // 상태 변경
        enrollment.changeStatus(EnrollmentStatus.CANCELLED);

        // 자리 차지한 경우만 감소 (WAITING은 자리 없음)
        if (prevStatus != EnrollmentStatus.WAITING) {
            enrollment.getClazz().decreaseCount();
        }

        // 자리 생긴 경우에만 대기열 승격
        if (prevStatus != EnrollmentStatus.WAITING) {

            Enrollment waiting = enrollmentRepository
                    .findFirstByClazzAndStatusOrderByCreatedAtAsc(
                            enrollment.getClazz(),
                            EnrollmentStatus.WAITING
                    );

            if (waiting != null) {
                // WAITING → CONFIRMED 상태 변경
                waiting.changeStatus(EnrollmentStatus.CONFIRMED);
                // 승격된 만큼 정원 증가
                enrollment.getClazz().increaseCount();
            }
        }

        // 항상 실행되도록 밖으로 이동
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

        // 4. WAITING 상태는 결제 불가
        if (enrollment.getStatus() == EnrollmentStatus.WAITING) {
            throw new RuntimeException("대기열 상태에서는 결제 불가");
        }

        // 5. 상태 변경
        enrollment.changeStatus(EnrollmentStatus.CONFIRMED);

        return enrollment;
    }

    // 특정 강의 대기열 인원 수 조회
    public long getWaitingCount(Long classId) {

        // 강의 조회
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        // WAITING 상태 개수 반환
        return enrollmentRepository.countByClazzAndStatus(
                clazz,
                EnrollmentStatus.WAITING
        );
    }
}