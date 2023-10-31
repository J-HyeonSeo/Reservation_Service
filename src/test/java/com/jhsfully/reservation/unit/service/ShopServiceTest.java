package com.jhsfully.reservation.unit.service;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_IS_DELETED;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_MATCH_USER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponse;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.impl.ShopServiceImpl;
import com.jhsfully.reservation.type.Days;
import com.jhsfully.reservation.type.SortingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/*
    Mockito를 사용하여, Unit테스트를 진행함.
 */

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ShopServiceImpl shopService;


    /*
        #######################################################################
        ###########                                                 ###########
        ###########           성공 케이스(SUCCESS CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 추가 성공")
    void addShopSuccess(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Member.builder()
                                .id(1L)
                                .build()
                ));
        given(shopRepository.save(any()))
                .willReturn(Shop.builder().id(1L).build());

        //when
        Long shopId = shopService.addShop(1L, ShopDto.AddShopRequest.builder()
                        .name("aaa")
                        .introduce("bbb")
                        .address("ccc")
                        .latitude(37.2)
                        .longitude(127.2)
                        .resOpenWeek(1)
                        .resOpenCount(2)
                        .resOpenDays(new ArrayList<>(Arrays.asList(Days.MON, Days.TUE, Days.WED)))
                        .resOpenTimes(new ArrayList<>(Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 30))))
                .build());
        //then
        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository, times(1)).save(captor.capture());
        Shop shop = captor.getValue();
        assertAll(
                () -> assertEquals(1L, shop.getMember().getId()),
                () -> assertEquals("aaa", shop.getName()),
                () -> assertEquals("bbb", shop.getIntroduce()),
                () -> assertEquals("ccc", shop.getAddress()),
                () -> assertEquals(37.2, shop.getLatitude()),
                () -> assertEquals(127.2, shop.getLongitude()),
                () -> assertEquals(1, shop.getResOpenWeek()),
                () -> assertEquals(2, shop.getResOpenCount()),
                () -> assertEquals(Arrays.asList(Days.MON, Days.TUE, Days.WED), shop.getResOpenDays()),
                () -> assertEquals(Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 30)), shop.getResOpenTimes())
        );
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 수정 성공")
    void updateShopSuccess(){
        //given
        Member member = Member.builder().id(1L).build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        member
                ));
        given(shopRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(
                                Shop.builder()
                                        .id(1L)
                                        .member(member)
                                        .build()
                        )
                );

        //when
        shopService.updateShop(1L, 1L, ShopDto.AddShopRequest.builder()
                .name("aaa")
                .introduce("bbb")
                .address("ccc")
                .latitude(37.2)
                .longitude(127.2)
                .resOpenWeek(1)
                .resOpenCount(2)
                .resOpenDays(new ArrayList<>(Arrays.asList(Days.MON, Days.TUE, Days.WED)))
                .resOpenTimes(new ArrayList<>(Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 30))))
                .build());
        //then
        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository, times(1)).save(captor.capture());
        Shop shop = captor.getValue();

        assertAll(
                () -> assertEquals("aaa", shop.getName()),
                () -> assertEquals("bbb", shop.getIntroduce()),
                () -> assertEquals("ccc", shop.getAddress()),
                () -> assertEquals(37.2, shop.getLatitude()),
                () -> assertEquals(127.2, shop.getLongitude()),
                () -> assertEquals(1, shop.getResOpenWeek()),
                () -> assertEquals(2, shop.getResOpenCount()),
                () -> assertEquals(Arrays.asList(Days.MON, Days.TUE, Days.WED), shop.getResOpenDays()),
                () -> assertEquals(Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 30)), shop.getResOpenTimes())
        );
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 삭제 성공 - (연관 데이터 X => 데이터 삭제")
    void deleteShopSuccessHard(){
        //given
        Member member = Member.builder().id(1L).build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        member
                ));
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Shop.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ));

        //when
        shopService.deleteShop(1L, 1L);

        //then
        verify(shopRepository, times(1)).delete(any());
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 삭제 성공 - (연관 데이터 O => 상태만 변경")
    void deleteShopSuccessSoft(){
        //given
        Member member = Member.builder().id(1L).build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        member
                ));
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Shop.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ));

        doThrow(new RuntimeException()).when(shopRepository).delete(any());

        //when
        shopService.deleteShop(1L, 1L);

        //then
        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository, times(1)).save(captor.capture());

        assertEquals(true, captor.getValue().isDeleted());
    }

    //네이티브 쿼리는 Mockito를 사용하여 대체함.
    @Test
    @DisplayName("[SERVICE]매장 검색 성공")
    void searchShopsSuccess(){

        //given
        given(shopRepository.findByNameAndOrdering(anyString(), anyDouble(), anyDouble(), any(), anyBoolean(), any()))
                .willReturn(
                        new PageImpl<>(
                                List.of(
                                    ShopTopResponse.builder()
                                        .id(1L)
                                        .name("name")
                                        .introduce("introduce")
                                        .address("address")
                                        .distance(2000)
                                        .star(0)
                                        .build()
                                )
                        )
                );

        //when
        Page<ShopTopResponse> results = shopService.searchShops(ShopDto.SearchShopParam.builder()
                .isAscending(true)
                .searchValue("")
                .latitude(38.0)
                .longitude(127.0)
                .sortingType(SortingType.DISTANCE)
                .build(), 0);

        //then
        assertAll(
                () -> assertEquals(1, results.getContent().size()),
                () -> assertEquals(1L, results.getContent().get(0).getId()),
                () -> assertEquals("name", results.getContent().get(0).getName()),
                () -> assertEquals("introduce", results.getContent().get(0).getIntroduce()),
                () -> assertEquals("address", results.getContent().get(0).getAddress()),
                () -> assertEquals(2000, results.getContent().get(0).getDistance()),
                () -> assertEquals(0, results.getContent().get(0).getStar())
        );
    }

    @Test
    @DisplayName("[SERVICE]매장 조회 for 파트너 - 성공")
    void getShopsByPartnerSuccess(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Member.builder().build()
                ));
        given(shopRepository.findByMemberAndIsDeletedFalse(any(), any()))
                .willReturn(new PageImpl<>(new ArrayList<>(Arrays.asList(
                        Shop.builder()
                                .id(2L)
                                .name("미용실")
                                .introduce("싹둑")
                                .address("서울")
                                .star(0)
                                .build()
                )), PageRequest.of(0, 10), 1));
        //when
        Page<ShopTopResponse> shops = shopService.getShopsByPartner(2L, 0);

        //then
        assertAll(
                () -> assertEquals(1, shops.getContent().size()),
                () -> assertEquals(2L, shops.getContent().get(0).getId()),
                () -> assertEquals("미용실", shops.getContent().get(0).getName()),
                () -> assertEquals("싹둑", shops.getContent().get(0).getIntroduce()),
                () -> assertEquals("서울", shops.getContent().get(0).getAddress()),
                () -> assertEquals(0, shops.getContent().get(0).getStar())
        );
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 성공")
    void getShopDetailForPartnerSuccess(){
        //given
        Member member = Member.builder().id(1L).build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        member
                ));
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Shop.builder()
                                .id(1L)
                                .member(member)
                                .name("빵집")
                                .introduce("빵")
                                .address("서울")
                                .star(0)
                                .resOpenWeek(1)
                                .resOpenCount(5)
                                .resOpenDays(new ArrayList<>(Arrays.asList(Days.MON, Days.TUE, Days.WED, Days.THU)))
                                .resOpenTimes(new ArrayList<>(Arrays.asList(
                                        LocalTime.of(9, 0),
                                        LocalTime.of(10, 00),
                                        LocalTime.of(11, 00),
                                        LocalTime.of(11, 30))))
                                .createdAt(LocalDateTime.of(2023,7,25, 0, 0))
                                .build()
                ));

        //when
        ShopDto.ShopDetailPartnerResponse shopDetail = shopService
                .getShopDetailForPartner(1L, 1L);

        //then
        assertAll(
                () -> assertEquals(1L, shopDetail.getId()),
                () -> assertEquals("빵집", shopDetail.getName()),
                () -> assertEquals("빵", shopDetail.getIntroduce()),
                () -> assertEquals(0, shopDetail.getStar()),
                () -> assertEquals("서울", shopDetail.getAddress()),
                () -> assertEquals(1, shopDetail.getResOpenWeek()),
                () -> assertEquals(5, shopDetail.getResOpenCount()),
                () -> assertEquals(Arrays.asList(Days.MON, Days.TUE, Days.WED, Days.THU), shopDetail.getResOpenDays()),
                () -> assertEquals(Arrays.asList(
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 00),
                        LocalTime.of(11, 00),
                        LocalTime.of(11, 30)), shopDetail.getResOpenTimes()),
                () -> assertEquals(LocalDateTime.of(2023,7,25, 0, 0), shopDetail.getCreatedAt()),
                () -> assertEquals(null, shopDetail.getUpdatedAt())
        );
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 상세 조회 for 유저 - 성공")
    void getShopDetailForUserSuccess(){
        //given
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Shop.builder()
                                .id(2L)
                                .name("미용실")
                                .introduce("싹둑")
                                .star(0)
                                .address("서울")
                                .resOpenWeek(1)
                                .resOpenCount(1)
                                .resOpenTimes(
                                        new ArrayList<>(
                                                Arrays.asList(
                                                        LocalTime.of(9,0)
                                                )
                                        )
                                )
                                .resOpenDays(
                                        new ArrayList<>(
                                                Arrays.asList(
                                                        Days.WED, Days.THU, Days.FRI
                                                )
                                        )
                                )
                                .build()
                ));

        //when
        ShopDto.ShopDetailUserResponse shopDetail = shopService
                .getShopDetailForUser(2L, LocalDate.of(2023, 7, 25));

        //then
        assertAll(
                () -> assertEquals(2L, shopDetail.getId()),
                () -> assertEquals("미용실", shopDetail.getName()),
                () -> assertEquals("싹둑", shopDetail.getIntroduce()),
                () -> assertEquals(0, shopDetail.getStar()),
                () -> assertEquals("서울", shopDetail.getAddress()),
                () -> assertEquals(3, shopDetail.getResOpenDateTimes().size()),
                () -> assertEquals(LocalTime.of(9, 0), shopDetail.getResOpenDateTimes().get(0).getReservationTimeSets().get(0).getTime()),
                () -> assertEquals(1, shopDetail.getResOpenDateTimes().get(0).getReservationTimeSets().get(0).getCount())
        );

    }

    @Nested
    @DisplayName("[SERVICE]별점 조작 테스트")
    class StarControlTest{
        @Test
        @DisplayName("[SERVICE]매장 별점 추가 성공")
        void addShopStarSuccess(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(
                            Optional.of(
                                    Shop.builder()
                                            .id(1L)
                                            .star(0)
                                            .starSum(0)
                                            .reviewCount(0)
                                            .build()
                            )
                    );

            //when
            shopService.addShopStar(1L, 5);
            shopService.addShopStar(1L, 3);
            //then
            ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
            verify(shopRepository, times(2)).save(captor.capture());
            Shop shop = captor.getValue();

            assertAll(
                    () -> assertEquals(4, shop.getStar()),
                    () -> assertEquals(2, shop.getReviewCount()),
                    () -> assertEquals(8, shop.getStarSum())
            );
        }

        @Test
        @DisplayName("[SERVICE]매장 별점 차감 성공")
        void subShopStarSuccess(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(
                            Optional.of(
                                    Shop.builder()
                                            .id(1L)
                                            .star(5)
                                            .starSum(10)
                                            .reviewCount(2)
                                            .build()
                            )
                    );
            //when
            shopService.subShopStar(1L, 5);

            //then
            ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
            verify(shopRepository, times(1)).save(captor.capture());
            Shop shop = captor.getValue();

            assertAll(
                    () -> assertEquals(5, shop.getStar()),
                    () -> assertEquals(1, shop.getReviewCount()),
                    () -> assertEquals(5, shop.getStarSum())
            );
        }

        @Test
        @DisplayName("[SERVICE]매장 별점 수정 성공")
        void updateShopStarSuccess(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(
                            Optional.of(
                                    Shop.builder()
                                            .id(1L)
                                            .star(5)
                                            .starSum(10)
                                            .reviewCount(2)
                                            .build()
                            )
                    );
            //when
            shopService.updateShopStar(1L, 5, 2);
            //then
            ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
            verify(shopRepository, times(1)).save(captor.capture());
            Shop shop = captor.getValue();

            assertAll(
                    () -> assertEquals(3.5, shop.getStar()),
                    () -> assertEquals(2, shop.getReviewCount()),
                    () -> assertEquals(7, shop.getStarSum())
            );
        }

    }

    /*
        #######################################################################
        ###########                                                 ###########
        ###########              실패 케이스(FAIL CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    //############################  addShop()  ##################################
    @Test
    @DisplayName("[SERVICE]매장 추가 - 실패 (유저 X)")
    void addShopFailUserNotFound(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> shopService.addShop(999L, ShopDto.AddShopRequest.builder().build()));

        //then
        assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
    }


    //############################  updateShop()  ################################

    @Nested
    @DisplayName("[SERVICE] 매장 수정 실패 케이스들")
    class updateShopFailCases{
        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (유저X)")
        void updateShopFailUserNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.updateShop(999L, 1L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장X)")
        void updateShopFailShopNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShop(1L, 999L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장 삭제 상태)")
        void updateShopFailShopIsDeleted(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().isDeleted(true).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShop(1L, 3L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장의 주인이 X)")
        void updateShopFailNotMatchUser(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().member(Member.builder().id(2L).build()).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShop(1L, 2L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }
    }

    //############################  deleteShop()  ################################
    @Nested
    @DisplayName("[SERVICE] 매장 삭제 실패 케이스들")
    class deleteShopFailCases{
        @Test
        @DisplayName("[SERVICE]매장 삭제 - 실패 (유저X)")
        void deleteShopFailUserNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.deleteShop(999L, 1L));

            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 삭제 - 실패 (매장X)")
        void deleteShopFailShopNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.deleteShop(1L, 999L));

            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 삭제 - 실패 (매장의 주인이 X)")
        void deleteShopFailNotMatchUser(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().member(Member.builder().id(2L).build()).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.deleteShop(1L, 2L));

            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }
    }

    //##########################  getShopsByPartner()  ###########################
    @Test
    @DisplayName("[SERVICE]매장 목록 조회 for 파트너 - 실패(유저 X)")
    void getShopsByPartnerFailUserNotFound(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> shopService.getShopsByPartner(999L, 0));
        //then
        assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
    }

    //#####################  getShopDetailForPartner()  ########################

    @Nested
    @DisplayName("[SERVICE]매장 상세 조회 for 파트너 실패 케이스들")
    class getShopDetailForPartnerFailCases{

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(유저 X)")
        void getShopDetailForPartnerFailUserNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.getShopDetailForPartner(999L, 1L));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 X)")
        void getShopDetailForPartnerFailShopNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForPartner(1L, 999L));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 삭제 상태)")
        void getShopDetailForPartnerFailShopIsDeleted(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().isDeleted(true).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForPartner(1L, 3L));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 소유자 일치 X)")
        void getShopDetailForPartnerFailNotMatchUser(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().member(Member.builder().id(2L).build()).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForPartner(1L, 2L));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }

    }

    //#######################  getShopDetailForUser()  ##########################

    @Nested
    @DisplayName("[SERVICE] 매장 상세 조회 for 유저 실패 케이스들")
    class getShopDetailForUserFailCases{
        @Test
        @DisplayName("[SERVICE] 매장 상세 조회 for 유저 - 실패(매장 X)")
        void getShopDetailForUserFailShopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForUser(999L, LocalDate.now()));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE] 매장 상세 조회 for 유저 - 실패(매장 삭제 상태)")
        void getShopDetailForUserFailShopIsDeleted(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().isDeleted(true).build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForUser(3L, LocalDate.now()));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }
    }

    //############################  ~ShopStar()  ################################
    @Nested
    @DisplayName("[SERVICE]별점 조작 실패 케이스들")
    class ControlShopStarFailCases{
        @Test
        @DisplayName("[SERVICE] 매장 별점 추가 - 실패(매장 X)")
        void addShopStarFailShopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.addShopStar(999L, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE] 매장 별점 차감 - 실패(매장 X)")
        void subShopStarFailShopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.subShopStar(999L, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE] 매장 별점 수정 - 실패(매장 X)")
        void updateShopStarFailShopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShopStar(999L, 3, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
    }
}