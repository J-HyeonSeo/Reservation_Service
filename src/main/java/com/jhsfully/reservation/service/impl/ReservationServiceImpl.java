package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ReservationException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.type.Days;
import com.jhsfully.reservation.type.ReservationState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.*;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_MATCH_USER;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;


    //예약을 수행함.
    @Override
    public void addReservation(Long memberId, ReservationDto.AddReservationRequest request) {

        //필요한 데이터 가져오기
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        //예약 가능 검증
        validateAddReservation(request, shop);

        //예약 수행
        Reservation reservation = Reservation.builder()
                .shop(shop)
                .member(member)
                .resDay(request.getResDay())
                .resTime(request.getResTime())
                .count(request.getCount())
                .reservationState(ReservationState.READY)
                .visited(false)
                .note(request.getNote())
                .build();

        reservationRepository.save(reservation);
    }

    @Override
    public List<ReservationDto.ReservationResponse> getReservationForUser(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        return reservationRepository.findByMember(member)
                .stream()
                .map(Reservation::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto.ReservationResponse> getReservationByShop(Long memberId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        return reservationRepository.findByShop(shop)
                .stream()
                .map(Reservation::toDto)
                .collect(Collectors.toList());
    }


    //=======================   검증 로직   ====================================

    private void validateAddReservation(ReservationDto.AddReservationRequest request, Shop shop){

        //예약 가능일 여부(오늘 + 1 ~ N주까지)
        // .minusDays는 isAfter함수가 자기 자신을 포함하지 않기 때문에 넣어주었음.
        if( !( request.getResDay().isAfter(LocalDate.now()) &&
                request.getResDay().isBefore(LocalDate.now().plusWeeks(shop.getResOpenWeek()).plusDays(1)) ) ){

            throw new ReservationException(RESERVATION_NOT_OPENED_DAY);
        }

        //오픈된 요일이 맞는지 확인
        boolean isGO = false;
        for(Days day : shop.getResOpenDays()){
            if(day.getValue() == request.getResDay().getDayOfWeek().getValue()){
                isGO = true;
                break;
            }
        }

        //오픈된 요일이 아닌 경우, Exception 날리기.
        if(!isGO){
            throw new ReservationException(RESERVATION_NOT_OPENED_DAY);
        }

        isGO = false;

        //오픈된 시간대 확인
        for(LocalTime time : shop.getResOpenTimes()){
            if(time.equals(request.getResTime())){
                isGO = true;
                break;
            }
        }

        if(!isGO){
            throw new ReservationException(RESERVATION_NOT_OPENED_TIME);
        }

        //reservation 들을 가져와서 해당 예약의 카운트를 확인.
        int alreadyReservedCount = reservationRepository
                .getReservationCountWithShopAndTime(shop, request.getResDay(), request.getResTime());

        if(alreadyReservedCount + request.getCount() > shop.getResOpenCount()){
            throw new ReservationException(RESERVATION_IS_OVERFLOW);
        }

    }
}
