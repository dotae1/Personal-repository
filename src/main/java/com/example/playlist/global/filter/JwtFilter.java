package com.example.playlist.global.filter;

import com.example.playlist.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.extractAccessToken(request).orElse(null);

        if( accessToken != null && jwtUtil.isTokenValid(accessToken)) {
            Optional<String> loginIdOpt = jwtUtil.extractLoginId(accessToken);

            if( loginIdOpt.isPresent()) {
                saveAuthentication(loginIdOpt.get());
            }

            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtUtil.extractRefreshToken(request).orElse(null);

        if( refreshToken != null) {
            Optional<String> loginIdOpt = jwtUtil.extractLoginId(refreshToken);

            if( loginIdOpt.isPresent() && jwtUtil.isRefreshTokenValid(refreshToken,  loginIdOpt.get())) {
                String newAccessToken = jwtUtil.createAccessToken(loginIdOpt.get());
                String newRefreshToken = jwtUtil.createRefreshToken(loginIdOpt.get());

                jwtUtil.updateRefreshToken(loginIdOpt.get(), newRefreshToken);
                jwtUtil.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

                log.info("AccessToken 재발급");
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(String loginId) {

        UserDetails userDetails = User.builder()
                .username(loginId)
                .password("")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
