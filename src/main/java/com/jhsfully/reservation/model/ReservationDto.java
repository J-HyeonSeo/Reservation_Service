package com.jhsfully.reservation.model;

import com.jhsfully.reservation.type.ReservationState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddReservationRequest{

        @NotNull(message = "매장 번호가 비어있습니다.")
        private Long shopId;
        @NotNull(message = "예약일이 비어있습니다.")
        private LocalDate resDay;
        @NotNull(message = "예약시간이 비어있습니다.")
        private LocalTime resTime;
        @Min(value = 1, message = "적어도 1명 이상 예약이 필요합니다.")
        private int count;
        private String note;

    }

    @Data
    @AllArgsConstructor
    public static class GetReservationParam{
        @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호 형식이 맞지 않습니다.")
        private String phone;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationResponse{
        private Long id;
        private String shopName;
        private LocalDate resDay;
        private LocalTime resTime;
        private int count;
        private ReservationState reservationState;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseForReview{
        private Long reservationId;
        private String shopName;
        private LocalDate visitDay;
        private LocalTime visitTime;

    }

}
