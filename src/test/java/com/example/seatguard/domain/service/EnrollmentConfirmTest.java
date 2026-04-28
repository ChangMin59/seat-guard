package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.enrollment.Enrollment;
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
class EnrollmentConfirmTest {

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    ClassRepository classRepository;

    // 1. 신청 없음
    @Test
    @DisplayName("존재하지 않는 신청 결제 시 예외")
    void confirmNotFoundTest() {
        assertThrows(RuntimeException.class, () -> {
            enrollmentService.confirm(999L);
        });
    }

    // 2. 정상 결제
    @Test
    @DisplayName("정상 결제 시 CONFIRMED 상태 변경")
    void confirmSuccessTest() {

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

        Enrollment confirmed = enrollmentService.confirm(e.getId());

        assertThat(confirmed.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    // 3. 이미 확정
    @Test
    @DisplayName("이미 CONFIRMED 상태는 재결제 불가")
    void alreadyConfirmedTest() {

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
        enrollmentService.confirm(e.getId());

        assertThrows(RuntimeException.class, () -> {
            enrollmentService.confirm(e.getId());
        });
    }

    // 4. 취소된 신청 결제 불가
    @Test
    @DisplayName("CANCELLED 상태는 결제 불가")
    void cancelledCannotConfirmTest() {

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
            enrollmentService.confirm(e.getId());
        });
    }

    // 5. WAITING 결제 불가
    @Test
    @DisplayName("WAITING 상태는 결제 불가")
    void waitingCannotConfirmTest() {

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

        assertThrows(RuntimeException.class, () -> {
            enrollmentService.confirm(waiting.getId());
        });
    }
}