package org.example.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-validity}")
    private long accessValidity;

    @Value("${jwt.refresh-validity}")
    private long refreshValidity;

    public String createAccessToken(Long userId) {
        return createToken(userId, accessValidity);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshValidity);
    }

    private String createToken(Long userId, long validity) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parserBuilder()
                        .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject());
    }
}
