package com.example.seatguard.domain.dto;

import com.example.seatguard.domain.classs.Class;
import lombok.Getter;

@Getter
public class ClassDetailResponseDto {

    private Long id;
    private String title;
    private String description;
    private int price;
    private int capacity;
    private int currentCount;
    private String status;

    public ClassDetailResponseDto(Class clazz) {
        this.id = clazz.getId();
        this.title = clazz.getTitle();
        this.description = clazz.getDescription();
        this.price = clazz.getPrice();
        this.capacity = clazz.getCapacity();
        this.currentCount = clazz.getCurrentCount();
        this.status = clazz.getStatus().name();
    }
}
