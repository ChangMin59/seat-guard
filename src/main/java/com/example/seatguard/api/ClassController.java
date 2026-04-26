package com.example.seatguard.api;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.seatguard.domain.classs.ClassStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;

    // 강의 등록
    @PostMapping
    public Class create(@RequestBody Class clazz) {
        return classService.create(clazz);
    }

    // 강의 목록 조회 (상태 필터 포함)
    @GetMapping
    public List<Class> getClasses(@RequestParam(required = false) ClassStatus status) {
        return classService.getClasses(status);
    }

    // 강의 상세 조회
    @GetMapping("/{id}")
    public Class getOne(@PathVariable Long id) {
        return classService.getOne(id);
    }
}
