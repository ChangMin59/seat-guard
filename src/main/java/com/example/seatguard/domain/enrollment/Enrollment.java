package com.example.seatguard.domain.enrollment;

import com.example.seatguard.domain.classs.Class;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 사용자 ID (간단 처리)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class clazz; // 어떤 강의인지

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private LocalDateTime createdAt;

    // 수강 상태 변경 (취소 등 상태 전이)
    public void changeStatus(EnrollmentStatus status) {

        // 전달받은 상태로 변경
        this.status = status;
    }
}
