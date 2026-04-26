package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.dto.ClassDetailResponseDto;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;

    // 강의 생성
    public Class create(Class clazz) {
        return classRepository.save(clazz);
    }

    // 상태 필터 포함 강의 목록 조회 (시간 기준 상태 반영 후 필터링)
    public List<Class> getClasses(ClassStatus status) {

        // 1. 전체 조회 후 상태 최신화
        List<Class> classes = classRepository.findAll().stream()
                .peek(c -> c.updateStatusByTime(LocalDateTime.now()))
                .toList();

        // 2. status가 있으면 메모리에서 필터링
        if (status != null) {
            return classes.stream()
                    .filter(c -> c.getStatus() == status)
                    .toList();
        }

        // 3. 없으면 전체 반환
        return classes;
    }

    // 강의 상세 DTO 조회
    public ClassDetailResponseDto getClassDetail(Long id) {
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        // DTO 변환 전에 상태 최신화 (시간 기준 상태 반영)
        clazz.updateStatusByTime(LocalDateTime.now());

        return new ClassDetailResponseDto(clazz);
    }
}