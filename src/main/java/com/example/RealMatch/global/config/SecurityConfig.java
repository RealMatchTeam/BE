package com.example.RealMatch.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.RealMatch.global.config.jwt.JwtAuthenticationFilter;
import com.example.RealMatch.global.presentation.advice.CustomAccessDeniedHandler;
import com.example.RealMatch.global.presentation.advice.CustomAuthEntryPoint;
import com.example.RealMatch.oauth.handler.OAuth2SuccessHandler;
import com.example.RealMatch.oauth.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private static final String[] PERMIT_ALL_URL_ARRAY = {
            "/", "/error", "/favicon.ico",
            "/css/**", "/js/**", "/images/**",
            "/login/**", "/oauth2/**",
            "/api/test",
            "/api/login/success",
            "/ws/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html",
            "/api/v1/tags/**",
            "/actuator/health"};

    private static final String[] REQUEST_AUTHENTICATED_ARRAY = {
            "/api/test-auth"
    };

    @Value("${swagger.server-url}")
    private String swaggerUrl;

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Value("${front.domain-url}")
    private String frontDomainUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(REQUEST_AUTHENTICATED_ARRAY).authenticated()
                        .requestMatchers(PERMIT_ALL_URL_ARRAY).permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failed", exception);
                            response.sendRedirect("/api/test?error=oauth_login_failed");
                        }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin, "http://localhost:8080", swaggerUrl, frontDomainUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
