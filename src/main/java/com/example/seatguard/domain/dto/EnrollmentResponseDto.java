package com.example.seatguard.domain.dto;

import com.example.seatguard.domain.enrollment.Enrollment;
import com.example.seatguard.domain.enrollment.EnrollmentStatus;
import lombok.Getter;

//수강 신청 응답 DTO
@Getter
public class EnrollmentResponseDto {

    private final Long enrollmentId;   // 수강 신청 ID
    private final Long classId;        // 강의 ID
    private final String classTitle;   // 강의명
    private final EnrollmentStatus status; // 신청 상태
    private String message; // 상태에 따른 안내 메시지

    //Entity → DTO 변환
    public EnrollmentResponseDto(Enrollment e) {
        this.enrollmentId = e.getId();
        this.classId = e.getClazz().getId();
        this.classTitle = e.getClazz().getTitle();
        this.status = e.getStatus();

        // 상태별 메시지 설정
        if (this.status == EnrollmentStatus.WAITING) {
            this.message = "정원이 초과되어 대기열에 등록되었습니다.";
        } else if (this.status == EnrollmentStatus.PENDING) {
            this.message = "수강 신청이 완료되었습니다.";
        } else if (this.status == EnrollmentStatus.CONFIRMED) {
            this.message = "수강이 확정되었습니다.";
        } else if (this.status == EnrollmentStatus.CANCELLED) {
            this.message = "수강 신청이 취소되었습니다.";
        } else {
            this.message = "";
        }
    }
}
