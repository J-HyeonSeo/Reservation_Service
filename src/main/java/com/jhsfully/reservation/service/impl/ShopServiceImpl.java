package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponseInterface;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final ReservationRepository reservationRepository;

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
    public List<ShopTopResponseInterface> searchShops(ShopDto.SearchShopParam param) {

        //비효율적인 기존 방식 일단 보류
//        List<Shop> shopList = shopRepository.findByNameStartingWith(param.getSearchValue());
//
//
//        List<ShopDto.ShopTopResponse> shopTopResponses = shopList.stream()
//                .map(x -> Shop.toTopResponse(x, param.getLatitude(), param.getLongitude()))
//                .collect(Collectors.toList());
//
//        switch (param.getSortingType()){
//            case TEXT:
//                Collections.sort(shopTopResponses,
//                        param.isAscending() ?
//                            Comparator.comparing(ShopDto.ShopTopResponse::getName) :
//                                Comparator.comparing(ShopDto.ShopTopResponse::getName, Comparator.reverseOrder())
//                );
//                break;
//            case STAR:
//                Collections.sort(shopTopResponses,
//                        param.isAscending() ?
//                                Comparator.comparing(ShopDto.ShopTopResponse::getStar) :
//                                Comparator.comparing(ShopDto.ShopTopResponse::getStar, Comparator.reverseOrder())
//                );
//                break;
//            case DISTANCE:
//                Collections.sort(shopTopResponses,
//                        param.isAscending() ?
//                                Comparator.comparing(ShopDto.ShopTopResponse::getDistance) :
//                                Comparator.comparing(ShopDto.ShopTopResponse::getDistance, Comparator.reverseOrder())
//                );
//                break;
//        }

        List<ShopTopResponseInterface> responses = shopRepository.findByNameAndOrdering(
                param.getSearchValue() + "%",
                param.getLatitude(),
                param.getLongitude(),
                param.getSortingType().name(),
                param.isAscending());

        return responses;
    }

    @Override
    public List<ShopDto.ShopTopResponse> getShopsByPartner(Long memberId, int pageIndex) {

        //검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Page<Shop> shopList = shopRepository.findByMember(member, PageRequest.of(pageIndex, 10));

        return shopList.getContent().stream()
                .map(x -> Shop.toTopResponse(x, -1, -1))
                .collect(Collectors.toList());
    }

    //파트너가 보는 매장 상세 데이터
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

    //유저가 보는 매장 상세 데이터.
    @Override
    public ShopDto.ShopDetailUserResponse getShopDetailForUser(Long shopId, LocalDate dateNow) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        //예약 관련 데이터 조회
        List<ShopDto.ReservationDateTimeSet> dateTimeSets = new ArrayList<>();

        LocalDate limitResDate = dateNow.plusWeeks(shop.getResOpenWeek());

        LocalDate presentDate = dateNow.plusDays(1); //오늘은 예약 가능일에 포함되지 말아야 함!
        while(!presentDate.isAfter(limitResDate)){

            ShopDto.ReservationDateTimeSet dateTimeSet = new ShopDto.ReservationDateTimeSet();
            dateTimeSet.setDate(presentDate);
            dateTimeSet.setReservationTimeSets(new ArrayList<>());

            for(LocalTime time : shop.getResOpenTimes()){
                int count = reservationRepository.getReservationCountWithShopAndTime(shop, presentDate, time);
                ShopDto.ReservationTimeSet timeSet = new ShopDto.ReservationTimeSet(time, shop.getResOpenCount() - count);
                dateTimeSet.getReservationTimeSets().add(timeSet);
            }

            dateTimeSets.add(dateTimeSet);
            presentDate = presentDate.plusDays(1);
        }

        //최종 빌드 및 리턴
        return ShopDto.ShopDetailUserResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .introduce(shop.getIntroduce())
                .star(shop.getStar())
                .address(shop.getAddress())
                .resOpenDateTimes(dateTimeSets)
                .build();
    }


}
