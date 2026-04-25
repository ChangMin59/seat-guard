package com.example.seatguard.domain.service;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
