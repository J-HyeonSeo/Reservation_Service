package com.jhsfully.reservation.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhsfully.reservation.config.SecurityConfiguration;
import com.jhsfully.reservation.controller.ReviewController;
import com.jhsfully.reservation.facade.ReviewFacade;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.security.JwtAuthenticationFilter;
import com.jhsfully.reservation.service.ReviewService;
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
import java.time.LocalDateTime;
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

@WebMvcTest(value = ReviewController.class, excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            SecurityConfiguration.class, JwtAuthenticationFilter.class
    })
)
public class ReviewControllerTest {

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ReviewFacade reviewFacade;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰를 위한 예약 조회")
    void getReservationForReviewTest() throws Exception {
        //given
        given(reviewService.getReservationsForReview(anyLong(), any(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ReservationDto.ResponseForReview.builder()
                                                .reservationId(1L)
                                                .reservationCount(1)
                                                .shopName("shopName")
                                                .visitDay(LocalDate.of(2023, 7, 15))
                                                .visitTime(LocalTime.of(9, 0))
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/review/reviewable/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].reservationCount").value(1))
                .andExpect(jsonPath("$[0].shopName").value("shopName"))
                .andExpect(jsonPath("$[0].visitDay").value("2023-07-15"))
                .andExpect(jsonPath("$[0].visitTime").value("09:00:00"));
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰 작성")
    void writeReviewTest() throws Exception {
        //when & then
        ReviewDto.WriteReviewRequest request = ReviewDto.WriteReviewRequest.builder()
                .star(4)
                .content("content")
                .build();
        mockMvc.perform(post("/review/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk());
        verify(reviewFacade, times(1)).writeReviewAndAddShopStar(any(), anyLong(), anyLong(), any());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰 수정")
    void updateReviewTest() throws Exception {
        //when & then
        ReviewDto.WriteReviewRequest request = ReviewDto.WriteReviewRequest.builder()
                .star(4)
                .content("content")
                .build();
        mockMvc.perform(put("/review/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk());
        verify(reviewFacade, times(1)).updateReviewAndUpdateShopStar(any(), anyLong(), anyLong(), any());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰 삭제")
    void deleteReviewTest() throws Exception {
        //when & then
        mockMvc.perform(delete("/review/1")
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk());
        verify(reviewFacade, times(1)).deleteReviewAndSubShopStar(anyLong(), anyLong());
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰 조회 for 유저")
    void getReviewsForUserTest() throws Exception {
        //given
        given(reviewService.getReviewsForUser(anyLong(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ReviewDto.ReviewResponse.builder()
                                                .id(1L)
                                                .reviewCount(1)
                                                .memberName("name")
                                                .star(5)
                                                .content("content")
                                                .createdAt(LocalDateTime.of(2023,7,15,9,0))
                                                .updatedAt(LocalDateTime.of(2023,7,15,9,0))
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/review/user/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reviewCount").value(1))
                .andExpect(jsonPath("$[0].memberName").value("name"))
                .andExpect(jsonPath("$[0].star").value(5))
                .andExpect(jsonPath("$[0].content").value("content"))
                .andExpect(jsonPath("$[0].createdAt").value("2023-07-15T09:00:00"))
                .andExpect(jsonPath("$[0].updatedAt").value("2023-07-15T09:00:00"));
    }


    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]리뷰 조회 For 매장")
    void getReviewsForShopTest() throws Exception {
        //given
        given(reviewService.getReviewsForShop(anyLong(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ReviewDto.ReviewResponse.builder()
                                                .id(1L)
                                                .reviewCount(1)
                                                .memberName("name")
                                                .star(5)
                                                .content("content")
                                                .createdAt(LocalDateTime.of(2023,7,15,9,0))
                                                .updatedAt(LocalDateTime.of(2023,7,15,9,0))
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/review/shop/1/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reviewCount").value(1))
                .andExpect(jsonPath("$[0].memberName").value("name"))
                .andExpect(jsonPath("$[0].star").value(5))
                .andExpect(jsonPath("$[0].content").value("content"))
                .andExpect(jsonPath("$[0].createdAt").value("2023-07-15T09:00:00"))
                .andExpect(jsonPath("$[0].updatedAt").value("2023-07-15T09:00:00"));
    }

}
