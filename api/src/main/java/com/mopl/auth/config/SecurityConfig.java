package com.mopl.auth.config;

import com.mopl.auth.filter.JwtAuthenticationFilter;
import com.mopl.auth.jwt.JwtTokenProvider;
import com.mopl.auth.redis.TokenRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize("hasRole('ADMIN')") 등 사용을 위해 필요
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // JwtAuthenticationFilter 는 @Component 가 아니라 여기서 직접 생성한다.
        // (Filter 빈으로 자동 스캔되면 @WebMvcTest slice 테스트에서 의존성을 못 찾아 컨텍스트 로딩이 깨짐)
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtTokenProvider, tokenRedisService);

        http
                // JWT 를 쿠키로 주고받으므로 CSRF 방어가 필요함.
                // 쿠키 이름 XSRF-TOKEN / 헤더 이름 X-XSRF-TOKEN 은 CookieCsrfTokenRepository 기본값과 정확히 일치.
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/reissue").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll() // 회원가입
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}