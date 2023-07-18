package com.jhsfully.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.List;

public class ShopDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddShopRequest{

        @NotBlank
        private String name;
        @NotBlank
        private String introduce;
        @NotBlank
        private String address;
        private double latitude;
        private double longitude;
        @Min(1)
        private int resOpenWeek;
        @Min(1)
        private int resOpenCount;
        private List<Integer> resOpenDays;
        private List<LocalTime> resOpenTimes;
    }

    public static class UpdateShopRequest{

    }

}
