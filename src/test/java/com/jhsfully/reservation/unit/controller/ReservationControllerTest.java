package com.jhsfully.reservation.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhsfully.reservation.config.SecurityConfiguration;
import com.jhsfully.reservation.controller.ReservationController;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.security.JwtAuthenticationFilter;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.type.ReservationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReservationController.class, excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            SecurityConfiguration.class, JwtAuthenticationFilter.class
    })
)
public class ReservationControllerTest {

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 추가 테스트")
    void addReservationTest() throws Exception {
        //when & then
        ReservationDto.AddReservationRequest request = ReservationDto
                .AddReservationRequest.builder()
                .shopId(1L)
                .resDay(LocalDate.now())
                .resTime(LocalTime.now())
                .count(1)
                .note("note")
                .build();
        mockMvc.perform(post("/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
        ).andDo(print())
                .andExpect(status().isOk());

        verify(reservationService, times(1)).addReservation(anyLong(), any(), any());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 삭제 테스트")
    void deleteReservationTest() throws Exception {
        //when & then
        mockMvc.perform(delete("/reservation/1").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(reservationService, times(1)).deleteReservation(anyLong(), anyLong());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 조회 for 유저 테스트")
    void getReservationsForUserTest() throws Exception {
        //given
        given(reservationService.getReservationForUser(anyLong(), any(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ReservationDto.ReservationResponse.builder()
                                                .reservationCount(1)
                                                .id(1L)
                                                .shopName("name")
                                                .resDay(LocalDate.of(2023, 7, 15))
                                                .resTime(LocalTime.of(9, 0))
                                                .count(1)
                                                .reservationState(ReservationState.READY)
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/reservation/user/0?startDate=2023-07-15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reservationCount").value(1))
                .andExpect(jsonPath("$[0].shopName").value("name"))
                .andExpect(jsonPath("$[0].resDay").value(LocalDate.of(2023, 7, 15).toString()))
                .andExpect(jsonPath("$[0].resTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].count").value(1))
                .andExpect(jsonPath("$[0].reservationState").value("READY"));
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 조회 for 매장 테스트")
    void getReservationsForShopTest() throws Exception {
        //given
        given(reservationService.getReservationByShop(anyLong(), anyLong(), any(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ReservationDto.ReservationResponse.builder()
                                                .reservationCount(1)
                                                .id(1L)
                                                .shopName("name")
                                                .resDay(LocalDate.of(2023, 7, 15))
                                                .resTime(LocalTime.of(9, 0))
                                                .count(1)
                                                .reservationState(ReservationState.READY)
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/reservation/partner/1/1?startDate=2023-07-15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reservationCount").value(1))
                .andExpect(jsonPath("$[0].shopName").value("name"))
                .andExpect(jsonPath("$[0].resDay").value(LocalDate.of(2023, 7, 15).toString()))
                .andExpect(jsonPath("$[0].resTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].count").value(1))
                .andExpect(jsonPath("$[0].reservationState").value("READY"));
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 거절 테스트")
    void rejectReservationTest() throws Exception {
        //when & then
        mockMvc.perform(patch("/reservation/reject/1").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(reservationService, times(1)).rejectReservation(anyLong(), anyLong(), any());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]예약 승인 테스트")
    void assignReservationTest() throws Exception {
        //when & then
        mockMvc.perform(patch("/reservation/assign/1").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(reservationService, times(1)).assignReservation(anyLong(), anyLong(), any());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]키오스크 예약 조회 테스트")
    void getReservationForVisitTest() throws Exception {
        //given
        given(reservationService.getReservationForVisit(anyLong(), anyLong(), any(), any(), any()))
                .willReturn(
                        ReservationDto.ReservationResponse.builder()
                                .reservationCount(1)
                                .id(1L)
                                .shopName("name")
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(9, 0))
                                .count(1)
                                .reservationState(ReservationState.READY)
                                .build()
                );
        //when & then
        mockMvc.perform(get("/reservation/kiosk/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reservationCount").value(1))
                .andExpect(jsonPath("$.shopName").value("name"))
                .andExpect(jsonPath("$.resDay").value(LocalDate.of(2023, 7, 15).toString()))
                .andExpect(jsonPath("$.resTime").value("09:00:00"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.reservationState").value("READY"));
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]키오스크 예약 방문 테스트")
    void visitShopByReservationTest() throws Exception {
        //when & then
        mockMvc.perform(patch("/reservation/kiosk/visit/1").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(reservationService, times(1)).visitReservation(anyLong(), anyLong(), any(), any());
    }
}
