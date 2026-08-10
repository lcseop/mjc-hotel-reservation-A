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
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .authorizeHttpRequests(this::authorizeApiRequests);

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
                .authorizeHttpRequests(this::authorizeApiRequests);

        return http.build();
    }

    private void authorizeApiRequests(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/error",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/oauth2/**",
                        "/login/oauth2/**"
                ).permitAll()
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/auth/oauth2/**",
                        "/api/mail/verification/**",
                        "/api/member/password-reset/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/term/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/member/email-exists").permitAll()
                .requestMatchers("/api/hotel/import/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/hotel/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/hotel/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/hotelame/**", "/api/hoteliname/**", "/api/hotelphoto/**", "/api/hoteltype/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/room/**", "/api/roomphoto/**", "/api/roomtype/**", "/api/roomtag/**", "/api/roomintag/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/review/**", "/api/review-photo/**", "/api/review-answer/**", "/api/review-tag-master/**", "/api/review-category-master/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/prom/search").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/hotel/**", "/api/hotelame/**", "/api/hoteliname/**", "/api/hotelphoto/**", "/api/hoteltype/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/hotel/**", "/api/hotelame/**", "/api/hoteliname/**", "/api/hotelphoto/**", "/api/hoteltype/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/hotel/**", "/api/hotelame/**", "/api/hoteliname/**", "/api/hotelphoto/**", "/api/hoteltype/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/room/**", "/api/roomphoto/**", "/api/roomtype/**", "/api/roomtag/**", "/api/roomintag/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/room/**", "/api/roomphoto/**", "/api/roomtype/**", "/api/roomtag/**", "/api/roomintag/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/room/**", "/api/roomphoto/**", "/api/roomtype/**", "/api/roomtag/**", "/api/roomintag/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/member").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/member/add", "/api/member-auth-accounts/**", "/api/member-term-agreements/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/member/**", "/api/member-auth-accounts/**", "/api/member-term-agreements/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/cou/member/**").authenticated()
                .requestMatchers("/api/cou/**").hasRole("ADMIN")
                .requestMatchers("/api/prom/**").hasRole("ADMIN")
                .requestMatchers("/api/refunds/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/email-logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/review-answer/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/review-answer/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/review-answer/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/review-tag-master/**", "/api/review-category-master/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/review-tag-master/**", "/api/review-category-master/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/review-tag-master/**", "/api/review-category-master/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/reservation/*/check-in", "/api/reservation/check-in/qr", "/api/reservation/*/check-out").hasRole("ADMIN")
                .requestMatchers("/api/payments/**", "/api/reservation/**", "/api/wish/**", "/api/review/**", "/api/review-photo/**", "/api/review-reaction/**").authenticated()
                .requestMatchers("/api/members/me/**", "/api/member/**", "/api/member-auth-accounts/**", "/api/member-term-agreements/**").authenticated()
                .anyRequest().authenticated();
    }

    private HttpSecurity configureCommonSecurity(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeSecurityError(response, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeSecurityError(response, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."))
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private void writeSecurityError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status, String message)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {
                  "code": "AUTHENTICATION_ERROR",
                  "message": "%s",
                  "data": null
                }
                """.formatted(message));
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
