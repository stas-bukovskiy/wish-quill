package com.wishquill.userservice.service;

import com.wishquill.userservice.dto.AuthenticationRequest;
import com.wishquill.userservice.exceptions.InvalidTokenException;
import com.wishquill.userservice.exceptions.UserNotFoundException;
import com.wishquill.userservice.models.User;
import com.wishquill.userservice.util.RandomModelUtil;
import com.wishquill.userservice.util.TestJwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = {AuthServiceImpl.class})
class AuthServiceTest {

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserService userService;

    @Autowired
    private AuthServiceImpl authService;


    @Test
    void testLoginUserWithValidRequest_shouldReturnToken() {
        final AuthenticationRequest authRequest = new AuthenticationRequest(RandomModelUtil.randomString(10), RandomModelUtil.randomString(10));
        final Authentication validAuth = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        final String generatedToken = TestJwtUtil.generateTestToken(RandomModelUtil.randomUser(), jwtSigningKey);

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(validAuth));
        when(jwtService.generateToken(validAuth)).thenReturn(generatedToken);

        Mono<String> stringMono = authService.loginUser(authRequest);

        StepVerifier.create(stringMono)
                .expectNext(generatedToken)
                .verifyComplete();
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(any());
    }

    @Test
    void testLoginUserWithInvalidRequest_shouldThrowException() {
        final AuthenticationRequest authRequest = new AuthenticationRequest(RandomModelUtil.randomString(10), RandomModelUtil.randomString(10));
        when(authenticationManager.authenticate(any())).thenReturn(Mono.error(new BadCredentialsException(RandomModelUtil.randomString(10))));

        Mono<String> stringMono = authService.loginUser(authRequest);

        StepVerifier.create(stringMono)
                .expectError(AuthenticationException.class)
                .verify();
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(0)).generateToken(any());
    }

    @Test
    void testVerifyTokenWithValidToken_shouldReturnUser() {
        final User user = RandomModelUtil.randomUser();
        final String userToken = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        when(jwtService.isTokenValid(userToken)).thenReturn(true);
        when(jwtService.extractUsername(userToken)).thenReturn(user.getUsername());
        when(userService.getUserByUsername(user.getUsername())).thenReturn(Mono.just(user));

        Mono<User> verifiedUser = authService.verifyToken(userToken);

        StepVerifier.create(verifiedUser)
                .expectNext(user)
                .verifyComplete();
        verify(jwtService, times(1)).isTokenValid(any());
        verify(jwtService, times(1)).extractUsername(any());
        verify(userService, times(1)).getUserByUsername(any());
    }

    @Test
    void testVerifyTokenWithInvalidToken_shouldThrowException() {
        final User user = RandomModelUtil.randomUser();
        final String userToken = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        when(jwtService.isTokenValid(userToken)).thenReturn(false);

        Mono<User> verifiedUser = authService.verifyToken(userToken);

        StepVerifier.create(verifiedUser)
                .verifyError(InvalidTokenException.class);
        verify(jwtService, times(1)).isTokenValid(any());
        verify(jwtService, times(0)).extractUsername(any());
        verify(userService, times(0)).getUserByUsername(any());
    }


    @Test
    void testVerifyTokenWithoutSuchUser_shouldThrowException() {
        final User user = RandomModelUtil.randomUser();
        final String userToken = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        when(jwtService.isTokenValid(userToken)).thenReturn(true);
        when(jwtService.extractUsername(userToken)).thenReturn(user.getUsername());
        when(userService.getUserByUsername(user.getUsername())).thenReturn(Mono.error(new UserNotFoundException("")));

        Mono<User> verifiedUser = authService.verifyToken(userToken);

        StepVerifier.create(verifiedUser)
                .verifyError(InvalidTokenException.class);
        verify(jwtService, times(1)).isTokenValid(any());
        verify(jwtService, times(1)).extractUsername(any());
        verify(userService, times(1)).getUserByUsername(any());
    }
}