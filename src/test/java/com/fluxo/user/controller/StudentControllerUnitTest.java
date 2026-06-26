package com.fluxo.user.controller;

import com.fluxo.auth.service.JwtService;
import com.fluxo.user.dto.AttendanceHistoryResponseDto;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@DisplayName("StudentController unit tests")
class StudentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StudentService studentService;

    @Test
    @DisplayName("GET /students/me/attendance returns attendance history")
    void getLoggedStudentAttendanceHistoryReturnsAttendanceHistory() throws Exception {
        AttendanceHistoryResponseDto response = new AttendanceHistoryResponseDto(List.of(
                new AttendanceHistoryResponseDto.AttendanceDayDto(
                        "2026-03-13",
                        List.of(
                                new AttendanceHistoryResponseDto.AttendanceSlotDto("19:15 - 20:45", "PRESENTE"),
                                new AttendanceHistoryResponseDto.AttendanceSlotDto("21:00 - 22:30", "AUSENTE")
                        )
                )
        ));

        when(studentService.getLoggedStudentAttendanceHistory()).thenReturn(response);

        mockMvc.perform(get("/students/me/attendance")
                        .with(user("aluno@fluxo.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.days[0].date").value("2026-03-13"))
                .andExpect(jsonPath("$.days[0].slots[0].time").value("19:15 - 20:45"))
                .andExpect(jsonPath("$.days[0].slots[0].status").value("PRESENTE"))
                .andExpect(jsonPath("$.days[0].slots[1].status").value("AUSENTE"));

        verify(studentService, times(1)).getLoggedStudentAttendanceHistory();
    }
}
