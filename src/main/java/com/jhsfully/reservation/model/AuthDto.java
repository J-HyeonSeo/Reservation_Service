package com.jhsfully.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpRequest{

        @Size(min = 5, message = "아이디는 최소 5자리 부터 입력해주세요.")
        private String username;
        @NotBlank(message = "이름이 비어있습니다.")
        private String name;
        @NotBlank(message = "비밀번호가 비어있습니다.")
        private String password;
        @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호 형식이 맞지 않습니다.")
        private String phone;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignInRequest{
        @NotBlank(message = "아이디가 비어있습니다.")
        private String username;
        @NotBlank(message = "비밀번호가 비어있습니다.")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest{
        @NotBlank(message = "토큰이 비어있습니다.")
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignInResponse{
        private String name;
        private String accessToken;
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class refreshResponse{
        private String accessToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        private Long id;
        private String username;
        private String password;
        private String name;
        private String phone;
        private boolean isPartner;
        private boolean isAdmin;
        private LocalDateTime registeredAt;
        private LocalDateTime updatedAt;
    }

}
