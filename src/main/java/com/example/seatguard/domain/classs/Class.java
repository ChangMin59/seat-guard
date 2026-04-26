package com.example.seatguard.domain.classs;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // 강의 제목

    private String description;  // 설명

    private int price;           // 가격

    private int capacity;        // 최대 정원

    private int currentCount;    // 현재 신청 인원 (핵심)

    @Enumerated(EnumType.STRING)
    private ClassStatus status;  // 상태

    private LocalDateTime startDate; // 시작일

    private LocalDateTime endDate;   // 종료일

    @Version
    private Long version; // 동시성 제어

    // 수강 신청 시 정원 증가 + 상태 자동 변경
    public void increaseCount() {

        // 정원 초과 방지
        if (this.currentCount >= this.capacity) {
            throw new RuntimeException("정원 초과");
        }

        // 인원 증가
        this.currentCount++;

        // 정원이 꽉 차면 CLOSED
        if (this.currentCount == this.capacity) {
            this.status = ClassStatus.CLOSED;
        }
    }

    // 수강 취소 시 정원 감소 + 상태 복구
    public void decreaseCount() {

        if (this.currentCount <= 0) {
            throw new RuntimeException("취소할 인원이 없음");
        }

        // 인원 감소
        this.currentCount--;

        // CLOSED 상태였다면 다시 OPEN으로 복구
        if (this.status == ClassStatus.CLOSED) {
            this.status = ClassStatus.OPEN;
        }
    }
}

