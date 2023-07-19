package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ShopDto;

import java.util.List;

public interface ShopService {

    void addShop(Long memberId, ShopDto.AddShopRequest request);

    void updateShop(Long memberId, ShopDto.UpdateShopRequest request);

    void deleteShop(Long memberId, Long shopId);

    List<ShopDto.ShopTopResponse> searchShops(ShopDto.SearchShopParam param);

    List<ShopDto.ShopTopResponse> getShopsByPartner(Long memberId);

    ShopDto.ShopDetailPartnerResponse getShopDetailForPartner(Long memberId, Long shopId);

}
