package com.example.playlist.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${spring.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String loginId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject("AccessToken")
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration * 1000))
                .signWith(getKey())
                .compact();
    }

    public String createRefreshToken(String loginId) {
        Date now = new Date();
        return Jwts.builder()
                .subject("RefreshToken")
                .claim("loginId", loginId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration * 1000))
                .signWith(getKey())
                .compact();
    }

    /** 소셜 신규 회원 - 추가정보 입력 전까지만 유효한 10분짜리 임시 토큰 */
    public String createTempToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject("TempToken")
                .claim("memberId", memberId)
                .claim("type", "TEMP")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 10 * 60 * 1000L))
                .signWith(getKey())
                .compact();
    }

    public void sendTempToken(HttpServletResponse response, String tempToken) {
        Cookie tempCookie = new Cookie("tempToken", tempToken);
        tempCookie.setHttpOnly(true);
        tempCookie.setPath("/");
        tempCookie.setMaxAge(10 * 60);
        response.addCookie(tempCookie);
        log.info("TempToken 쿠키 설정 완료");
    }

    public Optional<Long> extractMemberIdFromTempToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "tempToken".equals(c.getName()))
                .map(Cookie::getValue)
                .filter(this::isTokenValid)
                .findFirst()
                .flatMap(token -> {
                    try {
                        Claims claims = parseClaims(token);
                        if (!"TEMP".equals(claims.get("type", String.class))) return Optional.empty();
                        return Optional.ofNullable(claims.get("memberId", Long.class));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                });
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(accessTokenExpiration.intValue());
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/members/reissue");
        refreshCookie.setMaxAge(refreshTokenExpiration.intValue());
        response.addCookie(refreshCookie);

        log.info("Access, RefreshToken 쿠키 설정 완료");
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> extractLoginId(String token) {
        try {
            return Optional.ofNullable(
                    parseClaims(token).get("loginId", String.class)
            );
        } catch (Exception e) {
            log.info("loginId 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> extractRole(String token) {
        try {
            return Optional.ofNullable(
                    parseClaims(token).get("role", String.class)
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            if (redisTemplate.opsForValue().get("BlackList:" + token) != null) {
                log.info("로그아웃 된 토큰입니다.");
                return false;
            }
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            log.info("유효하지 않은 Token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, String loginId) {
        try {
            parseClaims(token);
            String storedToken = redisTemplate.opsForValue().get("RT:" + loginId);
            return storedToken != null && storedToken.equals(token);
        } catch (JwtException e) {
            log.error("유효하지 않은 RefreshToken: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void saveRefreshToken(String loginId, String refreshToken) {
        redisTemplate.opsForValue()
                .set("RT:" + loginId, refreshToken, Duration.ofSeconds(refreshTokenExpiration));
        log.info("RefreshToken 저장 - loginId={}", loginId);
    }

    @Transactional
    public void updateRefreshToken(String loginId, String refreshToken) {
        redisTemplate.opsForValue()
                .set("RT:" + loginId, refreshToken, Duration.ofSeconds(refreshTokenExpiration));
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, String loginId) {
        extractAccessToken(request).ifPresent(accessToken -> {
            try {
                Date expiration = parseClaims(accessToken).getExpiration();
                long expireMs = expiration.getTime() - System.currentTimeMillis();
                redisTemplate.opsForValue()
                        .set("BlackList:" + accessToken, loginId, Duration.ofMillis(expireMs));
                redisTemplate.delete("RT:" + loginId);
                log.info("로그아웃 처리 완료 - loginId={}", loginId);
            } catch (JwtException e) {
                log.warn("로그아웃 처리 중 토큰 파싱 실패: {}", e.getMessage());
            }
        });
        // 브라우저 쿠키 삭제
        clearCookie(response, "accessToken");
        clearCookie(response, "refreshToken");
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}