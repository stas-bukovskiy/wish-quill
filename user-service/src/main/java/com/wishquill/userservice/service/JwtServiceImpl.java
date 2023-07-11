package com.wishquill.userservice.service;

import com.wishquill.userservice.exceptions.InvalidTokenException;
import com.wishquill.userservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String AUTHORITIES_KEY = "roles";

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.expiration}")
    private Duration expiration;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Authentication extractAuthentication(String token) {
        Claims claims = extractAllClaims(token);
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                claims.get(AUTHORITIES_KEY).toString()
        );
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }

    @Override
    public String extractUsername(String token) {
        String username = extractClaim(token, Claims::getSubject);
        if (username == null) {
            throw new InvalidTokenException();
        }
        return username;
    }

    @Override
    public String generateToken(Authentication authentication) {
        long current = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(Map.of(AUTHORITIES_KEY, Strings.join(authentication.getAuthorities(), ',')))
                .setSubject(((User) authentication.getPrincipal()).getUsername())
                .setIssuedAt(new Timestamp(current))
                .setExpiration(new Timestamp(current + expiration.toMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return isTokenValidUtil(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenValidUtil(String token) {
        return extractExpiration(token).after(new Timestamp(System.currentTimeMillis()));
    }


    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
