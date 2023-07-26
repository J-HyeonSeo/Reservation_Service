package com.jhsfully.reservation.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhsfully.reservation.config.SecurityConfiguration;
import com.jhsfully.reservation.controller.ShopController;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponseInterface;
import com.jhsfully.reservation.security.JwtAuthenticationFilter;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.type.Days;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ShopController.class, excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            SecurityConfiguration.class, JwtAuthenticationFilter.class
    })
)
public class ShopControllerTest {

    @MockBean
    private ShopService shopService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("[Controller]매장 검색")
    void searchShopsTest() throws Exception {
        //given
        given(shopService.searchShops(any(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
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
                                                return 372000;
                                            }

                                            @Override
                                            public double getStar() {
                                                return 5;
                                            }
                                        }
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/shop/user/0?searchValue=n&sortingType=STAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shopCount").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[0].introduce").value("introduce"))
                .andExpect(jsonPath("$[0].address").value("address"))
                .andExpect(jsonPath("$[0].distance").value(372000))
                .andExpect(jsonPath("$[0].star").value(5));
    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 조회 By 파트너")
    void getShopsByPartnerTest() throws Exception {
        //given
        given(shopService.getShopsByPartner(anyLong(), anyInt()))
                .willReturn(
                        new ArrayList<>(
                                Arrays.asList(
                                        ShopDto.ShopTopResponse.builder()
                                                .id(1L)
                                                .name("name")
                                                .introduce("introduce")
                                                .star(5)
                                                .address("address")
                                                .build()
                                )
                        )
                );
        //when & then
        mockMvc.perform(get("/shop/partner/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[0].introduce").value("introduce"))
                .andExpect(jsonPath("$[0].star").value(5))
                .andExpect(jsonPath("$[0].address").value("address"));
    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 상세 조회 for 유저")
    void getShopDetailForUserTest() throws Exception {
        //given
        given(shopService.getShopDetailForUser(anyLong(), any()))
                .willReturn(
                        ShopDto.ShopDetailUserResponse.builder()
                                .id(1L)
                                .name("name")
                                .introduce("introduce")
                                .address("address")
                                .star(5)
                                .resOpenDateTimes(
                                        new ArrayList<>(
                                                Arrays.asList(
                                                        new ShopDto.ReservationDateTimeSet(
                                                                LocalDate.of(2023, 7, 15),
                                                                new ArrayList<>(
                                                                        Arrays.asList(
                                                                                new ShopDto.ReservationTimeSet(
                                                                                        LocalTime.of(9,0, 0),
                                                                                        5
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .build()
                );
        //when & then
        mockMvc.perform(get("/shop/user/detail/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.introduce").value("introduce"))
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.star").value(5))
                .andExpect(jsonPath("$.resOpenDateTimes[0].date").value(LocalDate.of(2023, 7, 15).toString()))
                .andExpect(jsonPath("$.resOpenDateTimes[0].reservationTimeSets[0].time").value("09:00:00"))
                .andExpect(jsonPath("$.resOpenDateTimes[0].reservationTimeSets[0].count").value(5));
    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 상세 조회 for 파트너")
    void getShopDetailForPartnerTest() throws Exception {
        //given
        given(shopService.getShopDetailForPartner(anyLong(), anyLong()))
                .willReturn(
                        ShopDto.ShopDetailPartnerResponse.builder()
                                .id(1L)
                                .name("name")
                                .introduce("introduce")
                                .star(5)
                                .address("address")
                                .resOpenWeek(1)
                                .resOpenCount(1)
                                .resOpenDays(new ArrayList<>(
                                        Arrays.asList(
                                                Days.MON
                                        )
                                ))
                                .resOpenTimes(new ArrayList<>(
                                        Arrays.asList(
                                                LocalTime.of(9, 0)
                                        )
                                ))
                                .createdAt(LocalDateTime.of(2023,7, 15, 9, 0))
                                .updatedAt(LocalDateTime.of(2023,7, 15, 9, 0))
                                .build()
                );
        //when & then
        mockMvc.perform(get("/shop/partner/detail/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.introduce").value("introduce"))
                .andExpect(jsonPath("$.star").value(5))
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.resOpenWeek").value(1))
                .andExpect(jsonPath("$.resOpenCount").value(1))
                .andExpect(jsonPath("$.resOpenDays[0]").value(Days.MON.name()))
                .andExpect(jsonPath("$.resOpenTimes[0]").value("09:00:00"))
                .andExpect(jsonPath("$.createdAt").value("2023-07-15T09:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2023-07-15T09:00:00"));

    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 추가")
    void addShopTest() throws Exception {

        //when & then
        ShopDto.AddShopRequest request = ShopDto.AddShopRequest.builder()
                .name("name")
                .introduce("introduce")
                .address("address")
                .latitude(37)
                .longitude(127)
                .resOpenWeek(1)
                .resOpenCount(1)
                .resOpenDays(
                        new ArrayList<>(
                                Arrays.asList(
                                        Days.MON
                                )
                        )
                )
                .resOpenTimes(
                        new ArrayList<>(
                                Arrays.asList(
                                        LocalTime.of(9, 0)
                                )
                        )
                )
                .build();

        mockMvc.perform(post("/shop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(shopService, times(1)).addShop(anyLong(),any());
    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 수정")
    void updateShopTest() throws Exception {
        //when & then
        ShopDto.AddShopRequest request = ShopDto.AddShopRequest.builder()
                .name("name")
                .introduce("introduce")
                .address("address")
                .latitude(37)
                .longitude(127)
                .resOpenWeek(1)
                .resOpenCount(1)
                .resOpenDays(
                        new ArrayList<>(
                                Arrays.asList(
                                        Days.MON
                                )
                        )
                )
                .resOpenTimes(
                        new ArrayList<>(
                                Arrays.asList(
                                        LocalTime.of(9, 0)
                                )
                        )
                )
                .build();
        mockMvc.perform(put("/shop/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(shopService, times(1)).updateShop(anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("[CONTROLLER]매장 삭제")
    void deleteShopTest() throws Exception {
        //when & then
        mockMvc.perform(delete("/shop/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(shopService, times(1)).deleteShop(anyLong(), anyLong());
    }
}
