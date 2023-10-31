package com.jhsfully.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopTopResponse {
    private Long id;
    private String name;
    private String introduce;
    private String address;
    private double distance;
    private double star;
}
