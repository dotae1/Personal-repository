package com.example.playlist.global.filter;

import com.example.playlist.global.util.JwtUtil;
import com.example.playlist.member.entity.Member;
import com.example.playlist.member.repository.MemberMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberMapper memberMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.extractAccessToken(request).orElse(null);

        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {
            Optional<String> loginIdOpt = jwtUtil.extractLoginId(accessToken);
            String role = jwtUtil.extractRole(accessToken).orElse("USER");

            if (loginIdOpt.isPresent()) {
                saveAuthentication(loginIdOpt.get(), role);
            }
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtUtil.extractRefreshToken(request).orElse(null);

        if (refreshToken != null) {
            Optional<String> loginIdOpt = jwtUtil.extractLoginId(refreshToken);

            if (loginIdOpt.isPresent() && jwtUtil.isRefreshTokenValid(refreshToken, loginIdOpt.get())) {
                Member member = memberMapper.findByLoginId(loginIdOpt.get()).orElse(null);
                String role = member != null && member.getRole() != null ? member.getRole().name() : "USER";

                String newAccessToken = jwtUtil.createAccessToken(loginIdOpt.get(), role);
                String newRefreshToken = jwtUtil.createRefreshToken(loginIdOpt.get());

                jwtUtil.updateRefreshToken(loginIdOpt.get(), newRefreshToken);
                jwtUtil.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);
                saveAuthentication(loginIdOpt.get(), role);

                log.info("AccessToken 재발급");
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(String loginId, String role) {
        UserDetails userDetails = User.builder()
                .username(loginId)
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
