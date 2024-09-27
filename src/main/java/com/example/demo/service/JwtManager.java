package com.example.demo.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtManager {

    @Value("${jwt.access-token.secret}")
    private String accessSecretString;

    @Value("${jwt.refresh-token.secret}")
    private String refreshSecretString;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey accessJwtSecret;
    private SecretKey refreshJwtSecret;

    @PostConstruct
    public void init() {
        this.accessJwtSecret = Keys.hmacShaKeyFor(accessSecretString.getBytes(StandardCharsets.UTF_8));
        this.refreshJwtSecret = Keys.hmacShaKeyFor(refreshSecretString.getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(Map<String, Object> claims, String subject, long jwtExpirationInMs, SecretKey jwtSecret) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public void validateAccessToken(String authToken) {
        Jwts.parserBuilder().setSigningKey(accessJwtSecret).build().parseClaimsJws(authToken);
    }

    public void validateRefreshToken(String authToken) {
        Jwts.parserBuilder().setSigningKey(refreshJwtSecret).build().parseClaimsJws(authToken);
    }

    public String generateAccessToken(String email) {
        return createToken(new HashMap<>(), email, accessTokenExpiration, accessJwtSecret);
    }

    public String generateRefreshToken(String email) {
        return createToken(new HashMap<>(), email, refreshTokenExpiration, refreshJwtSecret);
    }

    public String refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        String email = getEmailFromRefreshToken(refreshToken);
        return generateAccessToken(email);
    }

    public String getEmailFromRefreshToken(String refreshToken) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshJwtSecret)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();
    }
}