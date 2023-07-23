package com.jhsfully.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WriteReviewRequest{
        @Range(min = 1, max = 5, message = "별점은 1~5의 정수값이어야 합니다.")
        private int star;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse{
        private Long id;
        private long reviewCount;
        private String memberName;
        private double star;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    //========== 서비스 => 파서드 간 사용되는 Dto =============
    @Data
    @Builder
    public static class WriteReviewResponse{
        private Long reviewId;
        private Long shopId;
    }

    @Data
    @Builder
    public static class UpdateReviewResponse{
        private int originStar;
        private Long shopId;
    }

    @Data
    @Builder
    public static class DeleteReviewResponse{
        private int star;
        private Long reservationId;
        private Long shopId;
    }
}
