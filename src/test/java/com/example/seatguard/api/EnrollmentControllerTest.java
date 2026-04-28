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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class EnrollmentControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ClassRepository classRepository;

    private Long classId;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Class clazz = classRepository.save(
                Class.builder()
                        .title("테스트 강의")
                        .status(ClassStatus.OPEN)
                        .capacity(2)
                        .currentCount(0)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        classId = clazz.getId();
    }

    // 1. 수강 신청
    @Test
    @DisplayName("수강 신청 API 정상 호출")
    void enrollTest() throws Exception {

        mockMvc.perform(post("/enrollments")
                        .param("classId", classId.toString())
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }

    // 2. 수강 취소
    @Test
    @DisplayName("수강 취소 API 정상 호출")
    void cancelTest() throws Exception {

        // 먼저 신청
        String response = mockMvc.perform(post("/enrollments")
                        .param("classId", classId.toString())
                        .param("userId", "1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // id 파싱 (간단하게)
        String id = response.replaceAll("[^0-9]", "");

        mockMvc.perform(patch("/enrollments/" + id + "/cancel"))
                .andExpect(status().isOk());
    }
}