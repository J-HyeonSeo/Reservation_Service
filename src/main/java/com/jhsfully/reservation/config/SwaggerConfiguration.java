package com.jhsfully.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .securityContexts(Arrays.asList(securityContext("AccessToken"),
                        securityContext("RefreshToken")))
                .securitySchemes(Arrays.asList(apiKey("AccessToken"), apiKey("RefreshToken")))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jhsfully.reservation"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("매장 예약 서비스")
                .description("매장 예약 서비스 API문서")
                .version("1.0.0")
                .build();
    }

    private ApiKey apiKey(String name) {
        return new ApiKey(name, name, "header");
    }

    //전역 AuthorizationScope를 사용하여 JWT SecurityContext를 구성.
    private SecurityContext securityContext(String name) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth(name))
                .build();
    }

    private List<SecurityReference> defaultAuth(String name) {
        AuthorizationScope authorizationScope =
                new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes =
                new AuthorizationScope[1];

        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(
                new SecurityReference(name, authorizationScopes));
    }

}
