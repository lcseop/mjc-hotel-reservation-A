package com.mjc.hotel.config;

import com.mjc.hotel.auth.oauth.handler.OAuth2LoginFailureHandler;
import com.mjc.hotel.auth.oauth.handler.OAuth2LoginSuccessHandler;
import com.mjc.hotel.auth.oauth.service.SocialOAuth2UserService;
import com.mjc.hotel.auth.oauth.service.SocialOidcUserService;
import com.mjc.hotel.util.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final SocialOidcUserService socialOidcUserService;
    private final SocialOAuth2UserService socialOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
            authorizationCodeTokenResponseClient;

    @Bean
    @Profile("!oauth")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecurity(http)
                .authorizeHttpRequests(this::configureAuthorizationRules);

        return http.build();
    }

    @Bean
    @Profile("oauth")
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecurity(http)
                .oauth2Login(oauth2 -> oauth2
                        .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                .accessTokenResponseClient(authorizationCodeTokenResponseClient)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(socialOidcUserService)
                                .userService(socialOAuth2UserService)
                        )
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler(oauth2LoginFailureHandler)
                )
                .authorizeHttpRequests(this::configureAuthorizationRules);

        return http.build();
    }

    private void configureAuthorizationRules(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth
                // PUBLIC: 인증 없이 접근 가능한 인프라, 인증 및 조회 API
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/error",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/member/password-reset/**",
                        "/api/mail/verification/**"
                ).permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/member/password-reset").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/hotel/search").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/hotel/*",
                        "/api/hotel/iname/*",
                        "/api/hotel/inroom/*",
                        "/api/hotel/inreview/*",
                        "/api/hotel/inimage/*",
                        "/api/hotelphoto/**",
                        "/api/hotelame/**",
                        "/api/hoteliname/**",
                        "/api/hoteltype/**",
                        "/api/room/**",
                        "/api/roomphoto/**",
                        "/api/roomtype/**",
                        "/api/roomtag/**",
                        "/api/roomintag/**",
                        "/api/prom/**",
                        "/api/term/**",
                        "/api/review/**",
                        "/api/review-photo/**",
                        "/api/review-answer/**",
                        "/api/review-reaction/**",
                        "/api/review-category-master/**",
                        "/api/review-tag-master/**"
                ).permitAll()
                // ADMIN: 운영 및 관리 목적의 조회 API
                .requestMatchers(HttpMethod.GET,
                        "/api/member",
                        "/api/cou",
                        "/api/payments",
                        "/api/payments/**",
                        "/api/refunds",
                        "/api/refunds/**",
                        "/api/v1/email-logs",
                        "/api/v1/email-logs/**",
                        "/mapperStudents",
                        "/repositoryStudents"
                ).hasRole("ADMIN")
                // ADMIN: 운영 데이터 생성 API
                .requestMatchers(HttpMethod.POST,
                        "/api/member/add",
                        "/api/hotel",
                        "/api/hotelphoto/**",
                        "/api/hotelame/**",
                        "/api/hoteliname/**",
                        "/api/hoteltype/**",
                        "/api/room/**",
                        "/api/roomphoto/**",
                        "/api/roomtype/**",
                        "/api/roomtag/**",
                        "/api/roomintag/**",
                        "/api/prom/**",
                        "/api/term/**",
                        "/api/review-answer/**",
                        "/api/review-category-master/**",
                        "/api/review-tag-master/**",
                        "/api/cou",
                        "/api/cou/issue",
                        "/api/refunds/**",
                        "/api/v1/email-logs/**",
                        "/api/payments/add"
                ).hasRole("ADMIN")
                // ADMIN: 운영 데이터 변경 API
                .requestMatchers(HttpMethod.PATCH,
                        "/api/hotel/**",
                        "/api/hotelphoto/**",
                        "/api/hotelame/**",
                        "/api/hoteliname/**",
                        "/api/hoteltype/**",
                        "/api/room/**",
                        "/api/roomphoto/**",
                        "/api/roomtype/**",
                        "/api/roomtag/**",
                        "/api/roomintag/**",
                        "/api/prom/**",
                        "/api/term/**",
                        "/api/review-answer/**",
                        "/api/review-category-master/**",
                        "/api/review-tag-master/**",
                        "/api/cou/**",
                        "/api/refunds/**",
                        "/api/payments/**"
                ).hasRole("ADMIN")
                // ADMIN: 운영 데이터 삭제 API
                .requestMatchers(HttpMethod.DELETE,
                        "/api/member/**",
                        "/api/hotel/**",
                        "/api/hotelphoto/**",
                        "/api/hotelame/**",
                        "/api/hoteliname/**",
                        "/api/hoteltype/**",
                        "/api/room/**",
                        "/api/roomphoto/**",
                        "/api/roomtype/**",
                        "/api/roomtag/**",
                        "/api/roomintag/**",
                        "/api/prom/**",
                        "/api/term/**",
                        "/api/review-answer/**",
                        "/api/review-category-master/**",
                        "/api/review-tag-master/**",
                        "/api/cou/**",
                        "/api/refunds/**",
                        "/api/payments/**"
                ).hasRole("ADMIN")
                // AUTHENTICATED: 세부 소유권은 각 API의 @PreAuthorize에서 검증
                .anyRequest().authenticated();
    }

    private HttpSecurity configureCommonSecurity(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                    {"success":false,"message":"로그인이 필요합니다.","data":null}
                                    """);
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                    {"success":false,"message":"접근 권한이 없습니다.","data":null}
                                    """);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
