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

    //수강 신청 시 정원(현재 인원)을 증가시키는 메서드
    public void increaseCount() {

        // 현재 인원이 최대 정원 이상이면 더 이상 신청 불가
        if (this.currentCount >= this.capacity) {
            throw new RuntimeException("정원 초과");
        }

        // 정원 초과가 아니면 현재 인원을 1 증가
        this.currentCount++;
    }
}

