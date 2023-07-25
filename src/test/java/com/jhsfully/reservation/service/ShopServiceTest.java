package com.jhsfully.reservation.service;

import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponseInterface;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.impl.ShopServiceImpl;
import com.jhsfully.reservation.type.Days;
import com.jhsfully.reservation.type.SortingType;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/*
    ShopService 를 테스트하는 코드,
    H2 Database 를 사용하여, 통합테스트를 진행함.

    필요한 데이터는 .sql 파일을 통해 채워넣음.
 */

@SpringBootTest
class ShopServiceTest {

    @BeforeAll
    static void setup(@Autowired DataSource dataSource){
        try(Connection connection = dataSource.getConnection()){
            //데이터 초기화 및 추가
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("/testsqls/clean.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("/testsqls/member.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("/testsqls/shop.sql"));
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopRepository shopRepository;

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
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow();

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
    @DisplayName("[SERVICE]매장 수정 성공")
    void updateShopSuccess(){
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
        Shop shop = shopRepository.findById(1L)
                .orElseThrow();

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
    @DisplayName("[SERVICE]매장 삭제 성공")
    void deleteShopSuccess(){
        //when
        shopService.deleteShop(1L, 1L);

        //then
        assertThrows(ShopException.class,
                () -> shopService.deleteShop(1L, 1L));
    }

    //네이티브 쿼리는 Mockito를 사용하여 대체함.
    @Test
    @DisplayName("[SERVICE]매장 검색 성공")
    void searchShopsSuccess(){

        //mocking
        ShopRepository shopRepositoryMock = Mockito.mock(ShopRepository.class);
        ShopService shopServiceMock = new ShopServiceImpl(shopRepositoryMock, null, null);

        //given
        given(shopRepositoryMock.findByNameAndOrdering(anyString(), anyDouble(), anyDouble(), anyString(), anyBoolean(), anyLong(), anyLong()))
                .willReturn(
                        new ArrayList<>(
                                List.of(
                                        new ShopTopResponseInterface() {
                                            @Override
                                            public long getShopCount() {
                                                return 1;
                                            }

                                            @Override
                                            public Long getId() {
                                                return 1L;
                                            }

                                            @Override
                                            public String getName() {
                                                return "name";
                                            }

                                            @Override
                                            public String getIntroduce() {
                                                return "introduce";
                                            }

                                            @Override
                                            public String getAddress() {
                                                return "address";
                                            }

                                            @Override
                                            public double getDistance() {
                                                return 2000;
                                            }

                                            @Override
                                            public double getStar() {
                                                return 0;
                                            }
                                        }
                                )
                        )
                );

        //when
        List<ShopTopResponseInterface> results = shopServiceMock.searchShops(ShopDto.SearchShopParam.builder()
                .isAscending(true)
                .searchValue("")
                .latitude(38.0)
                .longitude(127.0)
                .sortingType(SortingType.DISTANCE)
                .build(), 0);

        //then
        assertAll(
                () -> assertEquals(1, results.size()),
                () -> assertEquals(1, results.get(0).getShopCount()),
                () -> assertEquals(1L, results.get(0).getId()),
                () -> assertEquals("name", results.get(0).getName()),
                () -> assertEquals("introduce", results.get(0).getIntroduce()),
                () -> assertEquals("address", results.get(0).getAddress()),
                () -> assertEquals(2000, results.get(0).getDistance()),
                () -> assertEquals(0, results.get(0).getStar())
        );
    }

    @Test
    @DisplayName("[SERVICE]매장 조회 for 파트너 - 성공")
    void getShopsByPartnerSuccess(){
        //when
        List<ShopDto.ShopTopResponse> shops = shopService.getShopsByPartner(2L, 0);

        //then
        assertAll(
                () -> assertEquals(1, shops.size()),
                () -> assertEquals(2L, shops.get(0).getId()),
                () -> assertEquals("미용실", shops.get(0).getName()),
                () -> assertEquals("싹둑", shops.get(0).getIntroduce()),
                () -> assertEquals("서울", shops.get(0).getAddress()),
                () -> assertEquals(0, shops.get(0).getStar())
        );
    }

    @Test
    @Transactional
    @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 성공")
    void getShopDetailForPartnerSuccess(){
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
                () -> assertEquals(7, shopDetail.getResOpenDateTimes().size())
        );

    }

    @Nested
    @DisplayName("[SERVICE]별점 조작 테스트")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StarControlTest{
        @Test
        @Order(1)
        @DisplayName("[SERVICE]매장 별점 추가 성공")
        void addShopStarSuccess(){
            //when
            shopService.addShopStar(1L, 5);
            shopService.addShopStar(1L, 3);
            //then
            Shop shop = shopRepository.findById(1L)
                    .orElseThrow();

            assertAll(
                    () -> assertEquals(4, shop.getStar()),
                    () -> assertEquals(2, shop.getReviewCount()),
                    () -> assertEquals(8, shop.getStarSum())
            );
        }

        @Test
        @Order(2)
        @DisplayName("[SERVICE]매장 별점 수정 성공")
        void updateShopStarSuccess(){
            //when
            shopService.updateShopStar(1L, 3, 2);
            //then
            Shop shop = shopRepository.findById(1L)
                    .orElseThrow();

            assertAll(
                    () -> assertEquals(3.5, shop.getStar()),
                    () -> assertEquals(2, shop.getReviewCount()),
                    () -> assertEquals(7, shop.getStarSum())
            );
        }

        @Test
        @Order(3)
        @DisplayName("[SERVICE]매장 별점 차감 성공")
        void subShopStarSuccess(){
            //when
            shopService.subShopStar(1L, 2);
            shopService.subShopStar(1L, 5);
            //then
            Shop shop = shopRepository.findById(1L)
                    .orElseThrow();

            assertAll(
                    () -> assertEquals(0, shop.getStar()),
                    () -> assertEquals(0, shop.getReviewCount()),
                    () -> assertEquals(0, shop.getStarSum())
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
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.updateShop(999L, 1L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장X)")
        void updateShopFailShopNotFound(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShop(1L, 999L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장 삭제 상태)")
        void updateShopFailShopIsDeleted(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShop(1L, 3L, ShopDto.AddShopRequest.builder().build()));

            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 수정 - 실패 (매장의 주인이 X)")
        void updateShopFailNotMatchUser(){
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
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.deleteShop(999L, 1L));

            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 삭제 - 실패 (매장X)")
        void deleteShopFailShopNotFound(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.deleteShop(1L, 999L));

            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 삭제 - 실패 (매장의 주인이 X)")
        void deleteShopFailNotMatchUser(){
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
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> shopService.getShopDetailForPartner(999L, 1L));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 X)")
        void getShopDetailForPartnerFailShopNotFound(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForPartner(1L, 999L));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 삭제 상태)")
        void getShopDetailForPartnerFailShopIsDeleted(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForPartner(1L, 3L));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 상세 조회 for 파트너 - 실패(매장 소유자 일치 X)")
        void getShopDetailForPartnerFailNotMatchUser(){
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
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.getShopDetailForUser(999L, LocalDate.now()));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE] 매장 상세 조회 for 유저 - 실패(매장 삭제 상태)")
        void getShopDetailForUserFailShopIsDeleted(){
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
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.addShopStar(999L, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE] 매장 별점 차감 - 실패(매장 X)")
        void subShopStarFailShopNotFound(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.subShopStar(999L, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE] 매장 별점 수정 - 실패(매장 X)")
        void updateShopStarFailShopNotFound(){
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> shopService.updateShopStar(999L, 3, 5));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
    }
}