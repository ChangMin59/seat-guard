package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.enrollment.Enrollment;
import com.example.seatguard.domain.enrollment.EnrollmentRepository;
import com.example.seatguard.domain.enrollment.EnrollmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class EnrollmentCancelTest {

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    ClassRepository classRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    // 1. 신청 없음
    @Test
    @DisplayName("존재하지 않는 신청 취소 시 예외")
    void cancelNotFoundTest() {
        assertThrows(RuntimeException.class, () -> {
            enrollmentService.cancel(999L);
        });
    }

    // 2. 이미 취소
    @Test
    @DisplayName("이미 취소된 신청은 다시 취소 불가")
    void alreadyCancelledTest() {

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(2)
                        .currentCount(0)
                        .status(ClassStatus.OPEN)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        Enrollment e = enrollmentService.enroll(clazz.getId(), 1L);
        enrollmentService.cancel(e.getId());

        assertThrows(RuntimeException.class, () -> {
            enrollmentService.cancel(e.getId());
        });
    }

    // 3. 강의 시작 후 취소 불가
    @Test
    @DisplayName("강의 시작 후 취소 불가")
    void cancelAfterStartTest() {

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(2)
                        .currentCount(0)
                        .status(ClassStatus.OPEN)
                        .startDate(LocalDateTime.now().minusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        Enrollment e = enrollmentService.enroll(clazz.getId(), 1L);

        assertThrows(RuntimeException.class, () -> {
            enrollmentService.cancel(e.getId());
        });
    }

    // 4. WAITING 취소 (count 변화 없음)
    @Test
    @DisplayName("WAITING 취소 시 count 변화 없음")
    void waitingCancelTest() {

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(1)
                        .currentCount(0)
                        .status(ClassStatus.OPEN)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        enrollmentService.enroll(clazz.getId(), 1L);
        Enrollment waiting = enrollmentService.enroll(clazz.getId(), 2L);

        enrollmentService.cancel(waiting.getId());

        Class updated = classRepository.findById(clazz.getId()).orElseThrow();

        assertThat(updated.getCurrentCount()).isEqualTo(1);
    }

    // 5. 일반 취소 (count 감소)
    @Test
    @DisplayName("취소 시 count 감소")
    void cancelDecreaseCountTest() {

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(2)
                        .currentCount(0)
                        .status(ClassStatus.OPEN)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        Enrollment e = enrollmentService.enroll(clazz.getId(), 1L);
        enrollmentService.enroll(clazz.getId(), 2L);

        enrollmentService.cancel(e.getId());

        Class updated = classRepository.findById(clazz.getId()).orElseThrow();

        assertThat(updated.getCurrentCount()).isEqualTo(1);
    }

    // 6. 대기열 승격
    @Test
    @DisplayName("취소 시 WAITING → CONFIRMED 승격")
    void promoteWaitingTest() {

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(1)
                        .currentCount(0)
                        .status(ClassStatus.OPEN)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        Enrollment e1 = enrollmentService.enroll(clazz.getId(), 1L);
        Enrollment waiting = enrollmentService.enroll(clazz.getId(), 2L);

        enrollmentService.cancel(e1.getId());

        Enrollment updated = enrollmentRepository.findById(waiting.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }
}