package com.jhsfully.reservation.unit.facade;

import com.jhsfully.reservation.facade.ReviewFacade;
import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.service.ShopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReviewFacadeTest {

    @Mock
    private ReviewService reviewService;
    @Mock
    private ShopService shopService;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ReviewFacade reviewFacade;

    @Test
    @DisplayName("[FACADE]리뷰 작성 및 별점 추가")
    void writeReviewAndAddShopStarTest(){
        //given
        given(reviewService.writeReview(any(), anyLong(), anyLong(), any()))
                .willReturn(
                        ReviewDto.WriteReviewResponse.builder()
                                .reviewId(1L)
                                .shopId(1L)
                                .build()
                );
        //when
        ReviewDto.WriteReviewRequest request = ReviewDto.WriteReviewRequest.builder()
                                                .star(5)
                                                .content("content")
                                                .build();
        reviewFacade.writeReviewAndAddShopStar(
                request,
                1L,
                1L,
                LocalDate.of(2023, 7, 15)
        );
        //then
        verify(reviewService, times(1)).writeReview(
                request, 1L, 1L, LocalDate.of(2023, 7, 15)
        );
        verify(reservationService, times(1)).setReview(
                1L, 1L
        );
        verify(shopService, times(1)).addShopStar(
                1L, 5
        );
    }

    @Test
    @DisplayName("[FACADE]리뷰 수정 및 별점 추가")
    void updateReviewAndUpdateShopStarTest(){
        //given
        given(reviewService.updateReview(any(), anyLong(), anyLong(), any()))
                .willReturn(
                        ReviewDto.UpdateReviewResponse.builder()
                                .originStar(5)
                                .shopId(1L)
                                .build()
                );
        //when
        ReviewDto.WriteReviewRequest request = ReviewDto.WriteReviewRequest.builder()
                .star(3)
                .content("content")
                .build();

        reviewFacade.updateReviewAndUpdateShopStar(
                request,
                1L,
                1L,
                LocalDate.of(2023, 7, 15)
        );
        //then
        verify(reviewService, times(1)).updateReview(
                request, 1L, 1L, LocalDate.of(2023, 7, 15)
        );
        verify(shopService, times(1)).updateShopStar(
                1L, 5, 3
        );
    }

    @Test
    @DisplayName("[FACADE]리뷰 삭제 및 별점 차감")
    void deleteReviewAndSubShopStarTest(){
        //given
        given(reviewService.getDataForDeleteReview(anyLong(), anyLong()))
                .willReturn(
                        ReviewDto.DeleteReviewResponse.builder()
                                .reservationId(2L)
                                .shopId(1L)
                                .star(5)
                                .build()
                );
        //when
        reviewFacade.deleteReviewAndSubShopStar(
            1L, 2L
        );
        //then
        verify(reservationService, times(1)).releaseReview(
                2L
        );
        verify(reviewService, times(1)).deleteReviewComplete(
                2L
        );
        verify(shopService, times(1)).subShopStar(
                1L, 5
        );
    }

}
