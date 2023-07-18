package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.type.AuthenticationErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDateTime;

import static com.jhsfully.reservation.type.AuthenticationErrorType.*;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;

    @Override
    public void addShop(Long memberId, ShopDto.AddShopRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = Shop.builder()
                .name(request.getName())
                .member(member)
                .introduce(request.getIntroduce())
                .star(0)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .resOpenWeek(request.getResOpenWeek())
                .resOpenCount(request.getResOpenCount())
                .resOpenDays(request.getResOpenDays())
                .resOpenTimes(request.getResOpenTimes())
                .createdAt(LocalDateTime.now())
                .build();

        shopRepository.save(shop);
    }

}
