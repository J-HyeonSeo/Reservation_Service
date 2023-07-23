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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.*;

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

    /*
        파트너가 자신의 shop을 수정하기 위해 사용되는 함수.
        id와 createdAt을 제외한, 나머지 값들이 한 번에 수정되므로,
        클라이언트에서 조회된 값을 그대로 다시 보내야하는 로직이 필요하다는 것을 의미한다.
        수정하고 싶은 데이터만 넣어서 보내게 되면, 나머지 값들이 없어짐에 유의해야함.
     */
    @Override
    public void updateShop(Long memberId, Long shopId, ShopDto.AddShopRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        //이미 삭제된 shop.
        if(shop.isDeleted()){
            throw new ShopException(SHOP_IS_DELETED);
        }

        //로그인된 member가 shop의 member와 일치하지 않는다면, 사용자가 매칭되지 않으므로 Throw Excepiton.
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

    /*
        파트너가 shop을 삭제하는 함수이다.
        FK에러가 발생하지 않는다면, 그냥 삭제를 수행하고,
        연관데이터가 발생할 경우에는, isDeleted를 true로 변경한다.

        즉, 다른 API에서 shop을 조회하는 것이 있을 경우, isDeleted가 false인 shop인 데이터만
        가져오게 해야함.
     */
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

    /*
        user가 shop들을 조회할 때 사용하는 함수임.
        검색어로 시작하는 단어를 매칭하여, 결과를 표시해줌. (검색어가 비어있다면, 모든 shop의 데이터가 나오게 됨.
        sortingType을 지정하여, TEXT(문자순), DISTANCE(거리순), STAR(별점) 순으로 정렬을 수행할 수 있음.
        isAsc의 값이 True이면, 지정된 sortingType필드를 기준으로 오름차순, False일 경우에는 내림차순으로 결과를 반환함.
        pageIndex를 사용하여, 10개씩 잘라서 응답하도록함.
     */
    @Override
    public List<ShopTopResponseInterface> searchShops(ShopDto.SearchShopParam param, int pageIndex) {

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

        /*
            쿼리문으로 searchValue로 시작하는 데이터를 검색하고,
            sortingType에 해당되는, 값을 기준으로 isAsc 기준에 맞게 정렬을 수행하고,
            이를 pageIndex로 페이징처리해서 반환함.
            nativeQuery를 수행하였기에, 반환값이 (interface)타입이 됨.
         */
        return shopRepository.findByNameAndOrdering(
                param.getSearchValue() + "%", //like 문을 사용하기 위한 % 전방 문자 탐색을 수행함 (INDEX을 사용하기 위함)
                param.getLatitude(),
                param.getLongitude(),
                param.getSortingType().name(),
                param.isAscending(),
                pageIndex * 10L, 10);
    }

    /*
        파트너가 등록한 shop들을 가져오는 함수임.
        memberId를 가져와서, 해당되는 memberId가 소유한 shop들을 반환하도록 함.
     */
    @Override
    public List<ShopDto.ShopTopResponse> getShopsByPartner(Long memberId, int pageIndex) {

        //검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Page<Shop> shopList = shopRepository.findByMemberAndIsDeletedFalse(member, PageRequest.of(pageIndex, 10));

        return shopList.getContent().stream()
                .map(Shop::toTopResponse)
                .collect(Collectors.toList());
    }

    /*
        파트너가 자신의 shop 데이터를 상세하게 볼 때 사용하는 함수임.
        이 때, memberId와 shop이 가지고 있는 memberId가 일치하지 않을 경우,
        자신이 소유한 shop이 아니므로, 에러를 반환하도록 함.
     */
    @Override
    public ShopDto.ShopDetailPartnerResponse getShopDetailForPartner(Long memberId, Long shopId) {

        //데이터 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        //shop이 삭제된 상태라면, Exception Throw
        if(shop.isDeleted()){
            throw new ShopException(SHOP_IS_DELETED);
        }

        //해당 shop이 로그인된 member 소유가 아니면 에러 Throw.
        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //응답 객체 빌드 및 리턴.
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

    /*
        유저가 해당 shop의 데이터를 조회할 때 사용되는 함수임.
        날짜별로 그룹핑하여, 시간대별로 예약가능한 count를 표시하여 유저에게 반환함.
     */
    @Override
    public ShopDto.ShopDetailUserResponse getShopDetailForUser(Long shopId, LocalDate dateNow) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        //shop이 삭제되었다면, Excepiton Throw
        if(shop.isDeleted()){
            throw new ShopException(SHOP_IS_DELETED);
        }

        //오픈된 요일 데이터 가져오기.
        HashSet<Integer> openDays = new HashSet<>();
        shop.getResOpenDays().stream()
                .forEach(x -> openDays.add(x.getValue()));

        //예약 관련 데이터 조회
        List<ShopDto.ReservationDateTimeSet> dateTimeSets = new ArrayList<>();

        LocalDate limitResDate = dateNow.plusWeeks(shop.getResOpenWeek()); //N주뒤 예약 일.

        LocalDate presentDate = dateNow.plusDays(1); //오늘은 예약 가능일에 포함되지 말아야 함!

        //presentDate가 하나씩 올라가면서, 날짜별로 데이터를 그룹핑함. ~ limit reservation date까지 수행함. 이를 넘으면 탈출.
        while(!presentDate.isAfter(limitResDate)){

            //오픈된 요일이 아닌 경우, 날짜만 올리고 다음으로 넘어감.
            if(!openDays.contains(presentDate.getDayOfWeek().getValue())){
                presentDate = presentDate.plusDays(1);
                continue;
            }

            //날짜에 그룹핑될 시간대 데이터를 생성함.
            ShopDto.ReservationDateTimeSet dateTimeSet = new ShopDto.ReservationDateTimeSet();
            dateTimeSet.setDate(presentDate);
            dateTimeSet.setReservationTimeSets(new ArrayList<>());

            //오픈된 시간대를 for문으로 순회하면서, 예약된 데이터를 확인하여, 이를 차감하여 제공함.
            for(LocalTime time : shop.getResOpenTimes()){
                int count = reservationRepository.getReservationCountWithShopAndTime(shop, presentDate, time);
                ShopDto.ReservationTimeSet timeSet = new ShopDto.ReservationTimeSet(time, shop.getResOpenCount() - count);
                dateTimeSet.getReservationTimeSets().add(timeSet);
            }

            //날짜 그룹핑 리스트에, 시간대데이터를 추가함.
            dateTimeSets.add(dateTimeSet);
            presentDate = presentDate.plusDays(1); //날짜를 하루 더함.
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
