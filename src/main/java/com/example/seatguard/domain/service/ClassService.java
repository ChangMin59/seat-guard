package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.seatguard.domain.classs.ClassStatus;
import com.example.seatguard.domain.dto.ClassDetailResponseDto;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;

    // 강의 생성
    public Class create(Class clazz) {
        return classRepository.save(clazz);
    }

    // 전체 조회
    public List<Class> getAll() {
        return classRepository.findAll();
    }

    // 단건 조회
    public Class getOne(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("강의 없음"));
    }

    // 상태 필터 포함 강의 목록 조회
    public List<Class> getClasses(ClassStatus status) {

        // status 없으면 전체 조회
        if (status == null) {
            return classRepository.findAll();
        }

        // status 있으면 필터 조회
        return classRepository.findByStatus(status);
    }

    // 강의 상세 DTO 조회 (추가)
    public ClassDetailResponseDto getClassDetail(Long id) {
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("강의 없음"));

        return new ClassDetailResponseDto(clazz);
    }
}
