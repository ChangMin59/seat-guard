package com.example.seatguard.api;

import com.example.seatguard.domain.classs.Class;
import com.example.seatguard.domain.classs.ClassRepository;
import com.example.seatguard.domain.classs.ClassStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ClassControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ClassRepository classRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // 1. 강의 목록 조회
    @Test
    @DisplayName("강의 목록 조회 API")
    void getClassesTest() throws Exception {

        classRepository.save(Class.builder()
                .title("테스트 강의")
                .status(ClassStatus.OPEN)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("테스트 강의"));
    }

    // 2. 강의 상세 조회
    @Test
    @DisplayName("강의 상세 조회 API")
    void getClassDetailTest() throws Exception {

        Class clazz = classRepository.save(Class.builder()
                .title("상세 강의")
                .status(ClassStatus.OPEN)
                .capacity(2)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        mockMvc.perform(get("/classes/{id}", clazz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("상세 강의"));
    }
}