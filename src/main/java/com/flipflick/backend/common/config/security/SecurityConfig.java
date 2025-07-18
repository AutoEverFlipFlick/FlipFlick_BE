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
                "http://localhost:8080"
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
                                "/api/v1/member/login", "/api/v1/member/signup", "/api/v1/member/reissue", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/h2-console/**", "/health").permitAll() // 회원, 스웨거, H2 인증 허가
                        .requestMatchers("/admin").hasRole("ADMIN") // 관리자 페이지 Role 체크
                        .requestMatchers("/api/v1/search/movie", "/api/v1/search/cast").permitAll() // 검색 인증 허가
                        .requestMatchers("/api/v1/movie/view", "/api/v1/cast/view").permitAll() // 영화, 배우 상세 조회 인증 허가
                        .requestMatchers("/api/v1/member/user-info", "/api/v1/member/user-info/*").permitAll()
                        .requestMatchers("/api/v1/member/user-info").permitAll()
                        .requestMatchers("/api/v1/playlist/**").permitAll()
                        .requestMatchers("/api/v1/s3/image").permitAll()
                        .requestMatchers("/api/v1/follow/**").permitAll()
                        .requestMatchers("/api/v1/member/kakao").permitAll()
                        .requestMatchers("/api/v1/member/naver").permitAll()
                        .requestMatchers("/api/v1/member/check/nickname").permitAll()
                        .requestMatchers("/api/v1/member/check/email").permitAll()
                        .requestMatchers("/api/v1/alarms/stream").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
