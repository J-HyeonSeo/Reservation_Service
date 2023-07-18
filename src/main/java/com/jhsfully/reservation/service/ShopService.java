package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ShopDto;

public interface ShopService {

    void addShop(Long memberId, ShopDto.AddShopRequest request);

}
