package com.wishquill.userservice.service;

import com.wishquill.userservice.exceptions.InvalidTokenException;
import com.wishquill.userservice.models.User;
import com.wishquill.userservice.util.RandomModelUtil;
import com.wishquill.userservice.util.TestJwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {JwtServiceImpl.class})
class JwtServiceTest {

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Autowired
    private JwtService jwtService;

    @Test
    void testGenerateToken() {
        final User user = RandomModelUtil.randomUser();
        final Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        String generatedToken = jwtService.generateToken(auth);

        assertThat(generatedToken).isNotNull();
        Claims claimsJws = TestJwtUtil.getJwtParser(jwtSigningKey).parseClaimsJws(generatedToken).getBody();
        assertThat(claimsJws.getSubject()).isEqualTo(user.getUsername());
        assertThat(claimsJws.get("roles")).isEqualTo(user.getRole().name());
    }

    @Test
    void testExtractAuthentication() {
        final User user = RandomModelUtil.randomUser();
        final String token = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        Authentication extractedAuth = jwtService.extractAuthentication(token);

        assertThat(extractedAuth).isNotNull();
        assertThat(extractedAuth.getPrincipal()).isEqualTo(user.getUsername());
        assertThat(extractedAuth.getAuthorities()).size().isEqualTo(1);
        assertThat(extractedAuth.getAuthorities()).element(0).isEqualTo(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Test
    void testExtractUsername() {
        final User user = RandomModelUtil.randomUser();
        final String token = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isNotNull();
        assertThat(extractedUsername).isEqualTo(user.getUsername());
    }

    @ParameterizedTest
    @MethodSource("tokenWithInvalidSignatureProvider")
    void testExtractUsernameWithInvalidToken_shouldThrowException(final String invalidToken) {
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void testIsTokenValidWithValidToken() {
        final String token = TestJwtUtil.generateTestToken(RandomModelUtil.randomUser(), jwtSigningKey);
        boolean isValid = jwtService.isTokenValid(token);
        assertThat(isValid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("tokenWithInvalidSignatureProvider")
    void testIsTokenValidWithInvalidToken_shouldReturnFalse(final String invalidToken) {
        boolean isValid = jwtService.isTokenValid(invalidToken);
        assertThat(isValid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("expiredTokenProvider")
    void testIsTokenValidWithExpiredToken_shouldThrowException(final String expiredToken) {
        boolean isValid = jwtService.isTokenValid(expiredToken);
        assertThat(isValid).isFalse();
    }

    private Stream<String> expiredTokenProvider() {
        return Stream.of(
                TestJwtUtil.generateTestToken(RandomModelUtil.randomUser(), -1000 * 60, jwtSigningKey),
                TestJwtUtil.generateTestToken(RandomModelUtil.randomUser(), 0, jwtSigningKey)
        );
    }

    private Stream<String> tokenWithInvalidSignatureProvider() {
        String token = TestJwtUtil.generateTestToken(RandomModelUtil.randomUser(), jwtSigningKey);
        return Stream.of(
                token.substring(0, token.length() - 2),
                token.substring(1, token.length() - 1),
                token.replace('a', 'b')
        );
    }


}