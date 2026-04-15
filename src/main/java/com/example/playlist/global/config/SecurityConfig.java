package com.example.playlist.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {return new BCryptPasswordEncoder();}

    /**
     * CSRF : Session 기반과는 다르게 Stateless하기 때문에 서버에 인증정보를 보관하지 않는다.
     * Rest Api에서는 요청에 필요한 정보를 포함시켜야한다. 따라서 불필요한 CSRF코드들을 작성할 필요가 없다.
     * FormLogin : 기본적으로 제공하는 FormLogin 커스텀으로 구현 가능
     * httpBasic : 특정 리소스에 대한 접근을 요청할 때 브라우저가 사용자에게 username과 password를 확인해 인가를 제한하는 방법
     * logout : GET /logout으로 접근이 가능하고, 로그아웃 필터를 거치지 않고 커스텀으로 구현이 가능
     * CORS :
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf((auth) -> auth.disable())
                .formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
                .logout((auth) -> auth.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource));
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/mail/**").permitAll()
                        .requestMatchers("/members/join").permitAll()
                        .requestMatchers("/members/login").permitAll()
                        .anyRequest().authenticated()
                );
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
