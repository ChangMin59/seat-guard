package com.example.seatguard.api;

import com.example.seatguard.domain.enrollment.Enrollment;
import com.example.seatguard.domain.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // REST API 컨트롤러 (JSON 형태로 응답)
@RequiredArgsConstructor // final 필드를 생성자로 자동 주입
@RequestMapping("/enrollments") // 기본 URL 경로 설정
public class EnrollmentController {

    // 수강 신청 비즈니스 로직 처리 서비스
    private final EnrollmentService enrollmentService;

    // 수강 신청 처리 (classId, userId 기반)
    @PostMapping
    public Enrollment enroll(

            // 요청 파라미터에서 classId 추출
            @RequestParam Long classId,

            // 요청 파라미터에서 userId 추출
            @RequestParam Long userId
    ) {
        // Service로 비즈니스 로직 위임
        // → 상태 체크, 정원 체크, 동시성 처리, Enrollment 생성 수행
        return enrollmentService.enroll(classId, userId);
    }
}
