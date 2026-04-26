package com.example.seatguard.domain.classs;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassRepository extends JpaRepository<Class, Long> {

    // 상태로 강의 조회
    List<Class> findByStatus(ClassStatus status);
}