package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;

    //사용자가 검색해서 조회하는 매장 정보 목록 조회
    @GetMapping("/user")
    public ResponseEntity<?> searchShops(@ModelAttribute ShopDto.SearchShopParam param){
        if(param.getSearchValue() == null){
            param.setSearchValue("");
        }
        List<ShopDto.ShopTopResponse> responses = shopService.searchShops(param);
        return ResponseEntity.ok(responses);
    }

    //파트너의 매장 정보 목록 조회
    @GetMapping("/partner")
    public ResponseEntity<?> getShopsByPartner(){
        Long memberId = MemberUtil.getMemberId();
        List<ShopDto.ShopTopResponse> responses = shopService.getShopsByPartner(memberId);
        return ResponseEntity.ok(responses);
    }

    //매장 상세 정보를 유저에게 제공함.(Reservation기능이 만들어져야 이어서 구현가능함)
    @GetMapping("/user/detail")
    public ResponseEntity<?> getShopDetailForUser(){
        return null;
    }

    //매장 상세 정보를 파트너에게 제공함.
    @GetMapping("/partner/detail/{shopId}")
    public ResponseEntity<?> getShopDetailForPartner(@PathVariable Long shopId){
        Long memberId = MemberUtil.getMemberId();
        ShopDto.ShopDetailPartnerResponse response = shopService.getShopDetailForPartner(memberId, shopId);
        return ResponseEntity.ok(response);
    }

    //매장을 추가함.
    @PostMapping
    public ResponseEntity<?> addShop(@RequestBody @Valid ShopDto.AddShopRequest request){
        Long memberId = MemberUtil.getMemberId();
        shopService.addShop(memberId, request);
        return ResponseEntity.ok().build();
    }

    //매장 정보를 수정함.
    @PutMapping
    public ResponseEntity<?> updateShop(@RequestBody @Valid ShopDto.UpdateShopRequest request){
        Long memberId = MemberUtil.getMemberId();
        shopService.updateShop(memberId, request);
        return ResponseEntity.ok().build();
    }

    //매장을 삭제함.
    @DeleteMapping("/{shopId}")
    public ResponseEntity<?> deleteShop(@PathVariable Long shopId){
        Long memberId = MemberUtil.getMemberId();
        shopService.deleteShop(memberId, shopId);
        return ResponseEntity.ok().build();
    }
}
