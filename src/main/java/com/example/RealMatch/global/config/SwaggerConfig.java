package com.example.RealMatch.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Profile;


@OpenAPIDefinition(
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080"),
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://139.150.81.226:8080")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    @Profile("local")
    public OpenAPI localOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080"))
                .info(new Info()
                        .title("🔗 RealMatch API (LOCAL)")
                        .version("1.0.0")
                        .description("로컬 개발용 API 문서"));
    }

    @Bean
    @Profile("prod")
    public OpenAPI prodOpenAPI() {
        return new OpenAPI()
                // 도메인 구매하면 url 변경하기
                .addServersItem(new Server().url("http://139.150.81.226:8080"))
                .info(new Info()
                        .title("🔗 RealMatch API")
                        .version("1.0.0")
                        .description("운영 서버 API 문서"));
    }
}