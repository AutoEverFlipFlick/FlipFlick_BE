package com.flipflick.backend.common.config.security;

import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.jwt.JWTFilter;
import com.flipflick.backend.common.jwt.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    public SecurityConfig(JWTUtil jwtUtil, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "https://www.flipflick.life",
                "http://localhost:5000",
                "https://api.flipflick.life"
        ));
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())

                // CSRF, form-login, basic-auth 모두 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)  // ← deprecated 아님 :contentReference[oaicite:0]{index=0}
                )

                .addFilterBefore(
                        new JWTFilter(jwtUtil, memberRepository),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 인가(Authorization) 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/member/login", "/api/v1/member/signup", "/api/v1/member/reissue", "/api/v1/member/logout","/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/h2-console/**", "/health", "/api-doc").permitAll() // 회원, 스웨거, H2 인증 허가
                        .requestMatchers("/api/v1/search/movie", "/api/v1/search/cast", "/api/v1/search/playlist", "/api/v1/search/member").permitAll() // 검색 인증 허가
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/search/movie", "/api/v1/search/cast").permitAll() // 검색 인증 허가
                        .requestMatchers("/api/v1/movie/view", "/api/v1/cast/view", "/api/v1/movie/top-popcorn").permitAll() // 영화, 배우 상세 조회 인증 허가
                        .requestMatchers("/api/v1/member/user-info", "/api/v1/member/user-info/*", "/api/v1/member/kakao", "/api/v1/member/naver", "/api/v1/member/check/nickname", "/api/v1/member/check/email").permitAll()
                        .requestMatchers("/api/v1/playlist/all","/api/v1/playlist/{playListId}", "/api/v1/playlist/**").permitAll()
                        .requestMatchers("/api/v1/s3/image").permitAll()
                        .requestMatchers("/api/v1/follow/**").permitAll()
                        .requestMatchers("/api/v1/movie/bookmark-list", "/api/v1/movie/watched-list").permitAll() // 찜, 봤어요 리스트 인증 허가
                        .requestMatchers("/api/v1/password-reset/**").permitAll() // 비밀번호 재설정 인증 허가
                        .requestMatchers("/api/v1/movie/bookmark-list", "/api/v1/movie/watched-list", "/api/v1/movie/like-list", "/api/v1/movie/boxoffice").permitAll() // 찜, 봤어요 리스트 인증 허가
                        .requestMatchers("/api/v1/alarms/**").permitAll()
                        .requestMatchers("/api/v1/popcorn/my", "/api/v1/popcorn/user/*").permitAll()
                        .requestMatchers("/api/v1/review/movie/**", "/api/v1/review/user/**").permitAll()
                        .requestMatchers("/api/v1/debate/user/**").permitAll()
                        .requestMatchers("/api/v1/recommendation/**").permitAll()
                        .requestMatchers("/api/v1/review/user/{nickname}/latest","/api/v1/review/movie/{tmdbId}/latest","/api/v1/review/movie/{tmdbId}/popular").permitAll()
                        .requestMatchers(GET, "/api/v1/debate/**").permitAll()
                        .requestMatchers(GET, "/api/v1/debate/movie/**").permitAll()
                        .requestMatchers(GET, "/api/v1/debate/comments/**").permitAll()
                        .anyRequest().authenticated()
                );


        return http.build();
    }
}
