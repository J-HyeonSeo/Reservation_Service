package com.jhsfully.reservation.model;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.type.SortingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ShopDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddShopRequest{

        @NotBlank(message = "매장 이름이 비어있습니다.")
        private String name;
        @NotBlank(message = "매장 소개가 비어있습니다.")
        private String introduce;
        @NotBlank(message = "매장 주소가 비어있습니다.")
        private String address;
        @NotNull(message = "위도값이 비어있습니다.")
        private double latitude;
        @NotNull(message = "경도값이 비어있습니다.")
        private double longitude;
        @Min(value = 1, message = "적어도 1주 이후까지의 예약을 받아야합니다.")
        private int resOpenWeek;
        @Min(value = 1, message = "동시간대에 적어도 1명은 예약을 받아야합니다.")
        private int resOpenCount;
        @NotEmpty(message = "예약이 오픈된 날이 없습니다.")
        @UniqueElements(message = "중복된 날이 존재합니다.")
        private List<Integer> resOpenDays;
        @NotEmpty(message = "예약이 오픈된 시간대가 없습니다.")
        @UniqueElements(message = "중복된 시간대가 존재합니다.")
        private List<LocalTime> resOpenTimes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateShopRequest{
        @NotNull
        private Long shopId;
        @NotBlank(message = "매장 이름이 비어있습니다.")
        private String name;
        @NotBlank(message = "매장 소개가 비어있습니다.")
        private String introduce;
        @NotBlank(message = "매장 주소가 비어있습니다.")
        private String address;
        @NotNull(message = "위도값이 비어있습니다.")
        private double latitude;
        @NotNull(message = "경도값이 비어있습니다.")
        private double longitude;
        @Min(value = 1, message = "적어도 1주 이후까지의 예약을 받아야합니다.")
        private int resOpenWeek;
        @Min(value = 1, message = "동시간대에 적어도 1명은 예약을 받아야합니다.")
        private int resOpenCount;
        @NotEmpty(message = "예약이 오픈된 날이 없습니다.")
        @UniqueElements(message = "중복된 날이 존재합니다.")
        private List<Integer> resOpenDays;
        @NotEmpty(message = "예약이 오픈된 시간대가 없습니다.")
        @UniqueElements(message = "중복된 시간대가 존재합니다.")
        private List<LocalTime> resOpenTimes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchShopParam{
        private String searchValue;
        private SortingType sortingType;
        private boolean isAscending;
        private double latitude;
        private double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShopTopResponse{

        private Long shopId;
        private String name;
        private String introduce;
        private String address;
        private double distance;
        private double star;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShopDetailUserResponse{
        private Long id;
        private String name;
        private String introduce;
        private double star;
        private String address;
        private int resOpenWeek;
        private int resOpenCount;
        private List<ReservationDateTimeSet> resOpenDateTime;

    }

    @Data
    @AllArgsConstructor
    public static class ReservationDateTimeSet{
        private LocalDate date;
        private LocalTime time;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShopDetailPartnerResponse{

        private Long id;
        private String name;
        private String introduce;
        private double star;
        private String address;
        private int resOpenWeek;
        private int resOpenCount;
        private List<Integer> resOpenDays;
        private List<LocalTime> resOpenTimes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }

}
