package com.example.RealMatch.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url}") String prodSwaggerUrl;

    @Bean
    @Profile("local")
    public OpenAPI localOpenAPI() {
        Info info = new Info()
                .title("🔗 RealMatch API (LOCAL)")
                .version("1.0.0")
                .description("ZzicGo API 명세서입니다.");

        String jwtSchemeName = "JWT Authentication";

        io.swagger.v3.oas.models.security.SecurityScheme securityScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, securityScheme);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement) // ✅ 모든 API에 전역적으로 인증 적용
                .components(components)
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }


    @Bean
    @Profile("prod")
    public OpenAPI prodOpenAPI() {
        Info info = new Info()
                .title("🔗 RealMatch API (PROD)")
                .version("1.0.0")
                .description("RealMatch Production API 명세서입니다.");

        String jwtSchemeName = "JWT Authentication";

        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList(jwtSchemeName);

        Components components =
                new Components().addSecuritySchemes(jwtSchemeName, securityScheme);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(
                        new Server()
                                .url(prodSwaggerUrl)
                                .description("Production Server")
                ));
    }


}