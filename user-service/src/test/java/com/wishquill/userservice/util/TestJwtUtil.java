package com.wishquill.userservice.util;

import com.wishquill.userservice.models.User;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.sql.Timestamp;
import java.util.Map;

public final class TestJwtUtil {

    private TestJwtUtil() {
    }

    public static JwtParser getJwtParser(String jwtSigningKey) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey(jwtSigningKey))
                .build();
    }

    public static Key getKey(String jwtSigningKey) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public static String generateTestToken(User user, String jwtSigningKey) {
        return generateTestToken(user, 1000 * 60, jwtSigningKey);
    }

    public static String generateTestToken(User user, long expiration, String jwtSigningKey) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(Map.of("roles", user.getRole()))
                .setSubject(user.getUsername())
                .setIssuedAt(new Timestamp(now))
                .setExpiration(new Timestamp(now + expiration))
                .signWith(getKey(jwtSigningKey), SignatureAlgorithm.HS256)
                .compact();
    }
}
