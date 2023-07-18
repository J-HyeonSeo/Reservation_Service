package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<?> addShop(@RequestBody @Valid ShopDto.AddShopRequest request){

        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        shopService.addShop(memberId, request);
        return ResponseEntity.ok().build();
    }

}
