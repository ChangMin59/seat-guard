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

    //Entity → DTO 변환
    public EnrollmentResponseDto(Enrollment e) {
        this.enrollmentId = e.getId();
        this.classId = e.getClazz().getId();
        this.classTitle = e.getClazz().getTitle();
        this.status = e.getStatus();
    }
}
