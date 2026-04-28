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
class EnrollmentEnrollTest {

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    ClassRepository classRepository;

    // 1. 정상 신청 (자리 있음 → PENDING)
    @Test
    @DisplayName("정원 여유 시 PENDING 상태로 신청")
    void enrollPendingTest() {

        // given
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

        // when
        Enrollment enrollment = enrollmentService.enroll(clazz.getId(), 1L);

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(clazz.getCurrentCount()).isEqualTo(1);
    }

    // 2. 정원 초과 (WAITING)
    @Test
    @DisplayName("정원 초과 시 WAITING 상태로 신청")
    void enrollWaitingTest() {

        // given
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

        // when
        Enrollment waiting = enrollmentService.enroll(clazz.getId(), 2L);

        // then
        assertThat(waiting.getStatus()).isEqualTo(EnrollmentStatus.WAITING);
    }

    // 3. DRAFT 상태 신청 불가
    @Test
    @DisplayName("DRAFT 상태에서는 신청 불가")
    void draftClassCannotEnrollTest() {

        // given
        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트")
                        .capacity(2)
                        .currentCount(0)
                        .status(ClassStatus.DRAFT)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        // then
        assertThrows(RuntimeException.class, () -> {
            enrollmentService.enroll(clazz.getId(), 1L);
        });
    }

    // 4. 중복 신청 방지
    @Test
    @DisplayName("중복 신청 시 예외 발생")
    void duplicateEnrollTest() {

        // given
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

        enrollmentService.enroll(clazz.getId(), 1L);

        // then
        assertThrows(RuntimeException.class, () -> {
            enrollmentService.enroll(clazz.getId(), 1L);
        });
    }
}
