package com.example.seatguard.api;

import com.example.seatguard.domain.enrollment.Enrollment;
import com.example.seatguard.domain.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.seatguard.domain.dto.EnrollmentResponseDto;
import java.util.List;

@RestController // REST API 컨트롤러 (JSON 형태로 응답)
@RequiredArgsConstructor // final 필드를 생성자로 자동 주입
@RequestMapping("/enrollments") // 기본 URL 경로 설정
public class EnrollmentController {

    // 수강 신청 비즈니스 로직 처리 서비스
    private final EnrollmentService enrollmentService;

    // 수강 신청 처리 (DTO로 메시지 포함 반환)
    @PostMapping
    public EnrollmentResponseDto enroll(
            @RequestParam Long classId,
            @RequestParam Long userId
    ) {
        // 서비스 실행
        Enrollment enrollment = enrollmentService.enroll(classId, userId);

        // DTO로 감싸서 반환 (status + message 포함)
        return new EnrollmentResponseDto(enrollment);
    }

    // 수강 신청 취소
    @PatchMapping("/{id}/cancel")
    public Enrollment cancel(@PathVariable Long id) {

        // Service에 취소 로직 위임
        return enrollmentService.cancel(id);
    }

    // 내 수강 신청 목록 조회 API
    @GetMapping("/my")
    public List<EnrollmentResponseDto> getMyEnrollments(@RequestParam Long userId) {
        return enrollmentService.getMyEnrollments(userId);
    }

    // 수강 확정(결제 완료) API
    @PatchMapping("/{id}/confirm")
    public Enrollment confirm(@PathVariable Long id) {
        return enrollmentService.confirm(id);
    }

    // 대기열 인원 수 조회 API
    @GetMapping("/{classId}/waiting-count")
    public long getWaitingCount(@PathVariable Long classId) {

        // 👉 Service 호출
        return enrollmentService.getWaitingCount(classId);
    }
}
