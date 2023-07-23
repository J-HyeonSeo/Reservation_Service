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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.*;
import static com.jhsfully.reservation.type.ReservationState.*;
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
    public void addReservation(Long memberId, ReservationDto.AddReservationRequest request, LocalDate dateNow) {

        //필요한 데이터 가져오기
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        //예약 가능 검증
        validateAddReservation(request, shop, member, dateNow);

        //예약 수행
        Reservation reservation = Reservation.builder()
                .shop(shop)
                .member(member)
                .resDay(request.getResDay())
                .resTime(request.getResTime())
                .count(request.getCount())
                .reservationState(READY)
                .note(request.getNote())
                .build();

        reservationRepository.save(reservation);
    }

    //자신이 예약한 정보를 가져오는 함수(start 날의 이후의 데이터를 paging 하여 가져옴)
    @Override
    public List<ReservationDto.ReservationResponse> getReservationForUser(Long memberId, LocalDate startDate, int pageIndex) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Page<Reservation> reservations = reservationRepository.findByMemberAndResDayGreaterThanEqual(member, startDate, PageRequest.of(pageIndex, 10));

        return reservations
                .getContent()
                .stream()
                .map(x -> Reservation.toDto(x, reservations.getTotalElements()))
                .collect(Collectors.toList());
    }

    //shop을 기준으로 파트너가 자신의 shop에 요청된 reservation들을 조회할 수 있음(start부터 조회되고, pageIndex로 페이징처리됨)
    @Override
    public List<ReservationDto.ReservationResponse> getReservationByShop(Long memberId, Long shopId, LocalDate startDate, int pageIndex) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        Page<Reservation> reservations = reservationRepository.findByShopAndResDayGreaterThanEqual(shop, startDate, PageRequest.of(pageIndex, 10));

        return reservations
                .getContent()
                .stream()
                .map(x -> Reservation.toDto(x, reservations.getTotalElements()))
                .collect(Collectors.toList());
    }

    /*
    예약을 삭제하는 함수임.
    단, 아무 예약이나 삭제해주진 않고, READY상태인 예약만 정상적으로 삭제함.
    EXPIRED(노쇼 및 파기), VISITED(방문완료), REJECT(거절) 상태는 삭제 불능이므로 에러 발생
    ASSIGN의 경우에는 => EXPIRED로 바꾸어 예약을 파기 했다는 사실을 알림.
     */
    @Override
    public void deleteReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        //검증이 필요함
        boolean isDelete = validateDeleteReservation(reservation, member);

        if(isDelete){ //데이터가 삭제되도 상관없음.
            reservationRepository.delete(reservation);
        }else{ //증거를 남기기 위해, 파기 처리만 수행함. 데이터를 삭제하지는 않음.
            reservation.setReservationState(EXPIRED);
            reservationRepository.save(reservation);
        }

    }

    //파트너가 자신의 shop에 들어온 예약을 거절하는 함수임. READY -> REJECT
    //당일날 해당되는 예약은 REJECT처리할 수 없음. (단, 스케줄러에 의해 이미 REJECT로 자동처리됨.)
    @Override
    public void rejectReservation(Long memberId, Long reservationId, LocalDate dateNow) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        validateRejectReservation(member, reservation, dateNow);

        reservation.setReservationState(REJECT);
        reservationRepository.save(reservation);
    }



    //파트너가 자신의 shop에 들어온 예약을 승인하는 함수임, READY -> ASSIGN
    //단, 오늘에 해당되는 예약은 승인 불가, (스케줄러에 의해, 당일날 예약이 READY시에는 자동으로 REJECT로 처리됨)
    @Override
    public void assignReservation(Long memberId, Long reservationId, LocalDate dateNow) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        validateAssignReservation(member, reservation, dateNow);

        reservation.setReservationState(ASSIGN);
        reservationRepository.save(reservation);
    }

    /*
        키오스크에서 예약정보를 조회하기 위해 사용되는 함수
        phone 넘버를 통해 가져옴.
        키오스크는 기본적으로, partner의 계정으로 로그인되어 있음.
        매개변수로 가져오는 memberId는 파트너의 멤버를 조회하고, shop과 매칭되는지 확인하기 위함임.
        visitMember 는 방문하려는 사람이, 조회하는 핸드폰 번호로 member를 찾아줌.
        reservation의 member는 reservation을 생성한 member의 데이터가 포함되어있음.
    */
    @Override
    public ReservationDto.ReservationResponse getReservationForVisit(Long memberId, Long shopId, ReservationDto.GetReservationParam param, LocalDate dateNow, LocalTime timeNow) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        //API가 조작되었는지 검증함.
        if(!Objects.equals(member.getId(), shop.getMember().getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //phone 넘버를 통해, member를 조회해야함.
        Member visitMember = memberRepository.findByPhone(param.getPhone())
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Reservation reservation = reservationRepository
                .findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(visitMember, shop, dateNow, timeNow)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        if(reservation.getReservationState() != ASSIGN){
            throw new ReservationException(RESERVATION_NOT_FOUND);
        }

        if(!timeNow.isAfter(reservation.getResTime().minusMinutes(10))){
            throw new ReservationException(RESERVATION_NOT_FOUND);
        }

        return Reservation.toDto(reservation, 1);
    }

    //방문 수행.
    @Override
    public void visitReservation(Long memberId, Long reservationId, LocalDate dateNow, LocalTime timeNow) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        validateForVisit(member, reservation, dateNow, timeNow);

        reservation.setReservationState(VISITED);
        reservationRepository.save(reservation);
    }


    //=======================   검증 로직   ====================================

    private void validateAddReservation(ReservationDto.AddReservationRequest request, Shop shop, Member member, LocalDate dateNow){

        //0명은 신청 불가능함
        if(request.getCount() == 0){
            throw new ReservationException(RESERVATION_CANNOT_ALLOW_ZERO);
        }

        //예약 가능일 여부(오늘 + 1 ~ N주뒤 까지)
        // .plusDays 는 isBefore()함수를 통해 범위를 계산할 때, N주뒤에 해당되는 날까지 포함하기 위함임.
        if( !( request.getResDay().isAfter(dateNow) &&
                request.getResDay().isBefore(dateNow.plusWeeks(shop.getResOpenWeek()).plusDays(1)) ) ){

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

        //오픈된 시간대가 아닌 경우 Exception 날리기
        if(!isGO){
            throw new ReservationException(RESERVATION_NOT_OPENED_TIME);
        }

        //같은 날에 이미 해당유저의 ASSIGN or READY 상태의 예약이 있으면 신청 못함
        int alreadyReservedCountForMember = reservationRepository.getReservationCountWithShopAndDayForMember(shop, member, request.getResDay());
        if(alreadyReservedCountForMember > 0){
            throw new ReservationException(RESERVATION_CANNOT_ALLOW_GREEDY_USER);
        }

        //reservation 들을 가져와서 해당 예약의 카운트를 확인.
        int alreadyReservedCount = reservationRepository
                .getReservationCountWithShopAndTime(shop, request.getResDay(), request.getResTime());

        //기존의 예약된 횟수에서 요청된 예약을 더하면, 허용하는 시간대별 예약 수용인원을 넘긴다면, 에러 발생.
        if(alreadyReservedCount + request.getCount() > shop.getResOpenCount()){
            throw new ReservationException(RESERVATION_IS_OVERFLOW);
        }

    }

    private boolean validateDeleteReservation(Reservation reservation, Member member){

        //해당 유저의 예약이 맞는가?
        if(!Objects.equals(reservation.getMember().getId(), member.getId())){
            throw new ReservationException(RESERVATION_NOT_MATCH_USER);
        }

        //파기된 상태, 방문한 상태, 거절된 상태는 증거를 남겨야 하므로 제거X
        if(reservation.getReservationState() == EXPIRED ||
            reservation.getReservationState() == VISITED ||
            reservation.getReservationState() == REJECT){
            throw new ReservationException(RESERVATION_CANNOT_DELETE);
        }

        //ASSIGN된 상태를 삭제하려고 하면, 삭제는 안되고, EXPIRED상태로 변경할거임.(예약 강제 취소 증거를 남기기위함)
        if(reservation.getReservationState() == ASSIGN){
            return false;
        }

        return true; //ready state
    }

    private void validateRejectReservation(Member member, Reservation reservation, LocalDate dateNow) {

        //해당 매장의 주인이 아님.(API 조작 가능성을 염두하여 체크해야함)
        if(!Objects.equals(reservation.getShop().getMember().getId(), member.getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //READY상태가 아닌, Reservation은 거절이 불가능함.
        if(reservation.getReservationState() != READY){
            throw new ReservationException(RESERVATION_CANNOT_REJECT_NOT_READY);
        }

        //스케줄러에서 새벽에 처리하지만, 작업 중에 요청이 발생할 수 있으므로 확인해야함.
        //오늘을 포함한, 이전 날짜에 대해서 처리가 불가하므로, plusDays(1)을 수행함.
        if(reservation.getResDay().isBefore(dateNow.plusDays(1))){
            throw new ReservationException(RESERVATION_CANNOT_REJECT_NOW_EQUAL_BEFORE);
        }

    }

    private void validateAssignReservation(Member member, Reservation reservation, LocalDate dateNow) {

        //해당 매장의 주인이 아님.(API 조작 가능성을 염두하여 체크해야함)
        if(reservation.getShop().getMember().getId() != member.getId()){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        //READY상태가 아닌, Reservation은 승인이 불가능함.
        if(reservation.getReservationState() != READY){
            throw new ReservationException(RESERVATION_CANNOT_ASSIGN_NOT_READY);
        }

        //스케줄러에서 새벽에 처리하지만, 작업 중에 요청이 발생할 수 있으므로 확인해야함.
        //오늘을 포함한, 이전 날짜에 대해서 처리가 불가하므로, plusDays(1)을 수행함.
        if(reservation.getResDay().isBefore(dateNow.plusDays(1))){
            throw new ReservationException(RESERVATION_CANNOT_ASSIGN_NOW_EQUAL_BEFORE);
        }

    }

    private void validateForVisit(Member member, Reservation reservation, LocalDate dateNow, LocalTime timeNow){
        if(!Objects.equals(reservation.getShop().getMember().getId(), member.getId())){
            throw new ShopException(SHOP_NOT_MATCH_USER);
        }

        if(reservation.getReservationState() != ASSIGN){
            throw new ReservationException(RESERVATION_CANNOT_VISIT_NOT_ASSIGN);
        }

        //방문일과 예약일이 일치하지 않음.
        if(!dateNow.equals(reservation.getResDay())){
            throw new ReservationException(RESERVATION_CANNOT_VISIT_DAY_NOT_EQUAL);
        }

        //현재 시간이 예약시간대의 이전이 아님.
        if(!timeNow.isBefore(reservation.getResTime())){
            throw new ReservationException(RESERVATION_CANNOT_VISIT_TIME_OVER);
        }
    }
}
