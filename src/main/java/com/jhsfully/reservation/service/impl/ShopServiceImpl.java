package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_MATCH_USER;

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
                .isDeleted(false)
                .build();

        shopRepository.save(shop);
    }

    @Override
    public void updateShop(Long memberId, ShopDto.UpdateShopRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //수정 로직 적용
        shop.setName(request.getName());
        shop.setIntroduce(request.getIntroduce());
        shop.setAddress(request.getAddress());
        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setResOpenWeek(request.getResOpenWeek());
        shop.setResOpenCount(request.getResOpenCount());
        shop.setResOpenDays(request.getResOpenDays());
        shop.setResOpenTimes(request.getResOpenTimes());
        shop.setUpdatedAt(LocalDateTime.now());

        shopRepository.save(shop);
    }

    @Override
    public void deleteShop(Long memberId, Long shopId) {

        //검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //삭제 수행
        try{
            shopRepository.delete(shop);
        }catch (Exception e){ //연관데이터가 있어 삭제가 안될 경우에는 비활성화로 넘겨버림.
            shop.setDeleted(true);
            shopRepository.save(shop);
        }

    }

    @Override
    public List<ShopDto.ShopTopResponse> searchShops(ShopDto.SearchShopParam param) {

        //검색어를 가지고 검색을 수행
        List<Shop> shopList = shopRepository.findByNameStartingWith(param.getSearchValue());

        /*
            거리 계산을 쿼리내에서 수행하지 않는 이유는, 쿼리문의 복잡성과 유지보수를 위함임.
            또한, 검색 대상의 결과는 전부 해야하기에 괜찮을 거임.
         */
        List<ShopDto.ShopTopResponse> shopTopResponses = shopList.stream()
                .map(x -> Shop.toTopResponse(x, param.getLatitude(), param.getLongitude()))
                .collect(Collectors.toList());

        /*
        정렬 또한, 유연한 정렬이 필요하기에, 쿼리가 아닌, 외부에서 수행함
         */

        switch (param.getSortingType()){
            case TEXT:
                Collections.sort(shopTopResponses,
                        param.isAscending() ?
                            Comparator.comparing(ShopDto.ShopTopResponse::getName) :
                                Comparator.comparing(ShopDto.ShopTopResponse::getName, Comparator.reverseOrder())
                );
                break;
            case STAR:
                Collections.sort(shopTopResponses,
                        param.isAscending() ?
                                Comparator.comparing(ShopDto.ShopTopResponse::getStar) :
                                Comparator.comparing(ShopDto.ShopTopResponse::getStar, Comparator.reverseOrder())
                );
                break;
            case DISTANCE:
                Collections.sort(shopTopResponses,
                        param.isAscending() ?
                                Comparator.comparing(ShopDto.ShopTopResponse::getDistance) :
                                Comparator.comparing(ShopDto.ShopTopResponse::getDistance, Comparator.reverseOrder())
                );
                break;
        }


        return shopTopResponses;
    }

    @Override
    public List<ShopDto.ShopTopResponse> getShopsByPartner(Long memberId) {

        //검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        List<Shop> shopList = shopRepository.findByMember(member);

        return shopList.stream()
                .map(x -> Shop.toTopResponse(x, -1, -1))
                .collect(Collectors.toList());
    }

    @Override
    public ShopDto.ShopDetailPartnerResponse getShopDetailForPartner(Long memberId, Long shopId) {

        //데이터 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        return ShopDto.ShopDetailPartnerResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .introduce(shop.getIntroduce())
                .star(shop.getStar())
                .address(shop.getAddress())
                .resOpenWeek(shop.getResOpenWeek())
                .resOpenCount(shop.getResOpenCount())
                .resOpenDays(shop.getResOpenDays())
                .resOpenTimes(shop.getResOpenTimes())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }


}
