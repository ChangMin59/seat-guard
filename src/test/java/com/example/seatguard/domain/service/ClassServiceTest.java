package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.dto.ClassDetailResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ClassServiceTest {

    @Autowired
    ClassService classService;

    @Autowired
    ClassRepository classRepository;

    // 1. 전체 조회
    @Test
    @DisplayName("전체 강의 조회")
    void getAllClassesTest() {

        // given
        classRepository.save(Class.builder()
                .title("테스트1")
                .status(ClassStatus.OPEN)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        // when
        List<Class> result = classService.getClasses(null);

        // then
        assertThat(result).isNotEmpty();
    }

    // 2. 상태 필터링
    @Test
    @DisplayName("상태 기준 필터링")
    void filterByStatusTest() {

        // given
        classRepository.save(Class.builder()
                .title("OPEN 강의")
                .status(ClassStatus.OPEN)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        classRepository.save(Class.builder()
                .title("DRAFT 강의")
                .status(ClassStatus.DRAFT)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        // when
        List<Class> result = classService.getClasses(ClassStatus.OPEN);

        // then
        assertThat(result).allMatch(c -> c.getStatus() == ClassStatus.OPEN);
    }

    // 3. 상세 조회
    @Test
    @DisplayName("강의 상세 조회")
    void getClassDetailTest() {

        // given
        Class clazz = classRepository.save(Class.builder()
                .title("테스트")
                .status(ClassStatus.OPEN)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        // when
        ClassDetailResponseDto result = classService.getClassDetail(clazz.getId());

        // then
        assertThat(result).isNotNull();
    }

    // 4. 존재하지 않는 강의
    @Test
    @DisplayName("없는 강의 조회 시 예외 발생")
    void classNotFoundTest() {

        // then
        assertThrows(RuntimeException.class, () -> {
            classService.getClassDetail(999L);
        });
    }
}