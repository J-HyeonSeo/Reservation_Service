package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponseInterface;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;

    //사용자가 검색해서 조회하는 매장 정보 목록 조회
    @GetMapping("/user/{pageIndex}")
    public ResponseEntity<?> searchShops(@PathVariable int pageIndex, @ModelAttribute ShopDto.SearchShopParam param){
        if(param.getSearchValue() == null){
            param.setSearchValue("");
        }
        List<ShopTopResponseInterface> responses = shopService.searchShops(param, pageIndex);
        return ResponseEntity.ok(responses);
    }

    //파트너의 매장 정보 목록 조회
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/partner/{pageIndex}")
    public ResponseEntity<?> getShopsByPartner(@PathVariable int pageIndex){
        Long memberId = MemberUtil.getMemberId();
        List<ShopDto.ShopTopResponse> responses = shopService.getShopsByPartner(memberId, pageIndex);
        return ResponseEntity.ok(responses);
    }

    //매장 상세 정보를 유저에게 제공함.(Reservation기능이 만들어져야 이어서 구현가능함)
    @GetMapping("/user/detail/{shopId}")
    public ResponseEntity<ShopDto.ShopDetailUserResponse> getShopDetailForUser(@PathVariable Long shopId){
        ShopDto.ShopDetailUserResponse response = shopService.getShopDetailForUser(shopId, LocalDate.now());
        return ResponseEntity.ok(response);
    }

    //매장 상세 정보를 파트너에게 제공함.
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/partner/detail/{shopId}")
    public ResponseEntity<?> getShopDetailForPartner(@PathVariable Long shopId){
        Long memberId = MemberUtil.getMemberId();
        ShopDto.ShopDetailPartnerResponse response = shopService.getShopDetailForPartner(memberId, shopId);
        return ResponseEntity.ok(response);
    }

    //매장을 추가함.
    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping
    public ResponseEntity<?> addShop(@RequestBody @Valid ShopDto.AddShopRequest request){
        Long memberId = MemberUtil.getMemberId();
        shopService.addShop(memberId, request);
        return ResponseEntity.ok().build();
    }

    //매장 정보를 수정함.
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/{shopId}")
    public ResponseEntity<?> updateShop(@PathVariable Long shopId, @RequestBody @Valid ShopDto.AddShopRequest request){
        Long memberId = MemberUtil.getMemberId();
        shopService.updateShop(memberId, shopId, request);
        return ResponseEntity.ok().build();
    }

    //매장을 삭제함.
    @PreAuthorize("hasRole('PARTNER')")
    @DeleteMapping("/{shopId}")
    public ResponseEntity<?> deleteShop(@PathVariable Long shopId){
        Long memberId = MemberUtil.getMemberId();
        shopService.deleteShop(memberId, shopId);
        return ResponseEntity.ok().build();
    }
}
