package com.wishquill.userservice.controllers;

import com.wishquill.userservice.dto.AuthenticationRequest;
import com.wishquill.userservice.dto.UserDto;
import com.wishquill.userservice.models.User;
import com.wishquill.userservice.repositories.UserRepository;
import com.wishquill.userservice.util.RandomModelUtil;
import com.wishquill.userservice.util.TestJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @Value("${token.signing.key}")
    private String jwtSigningKey;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebTestClient webClient;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User user;

    @BeforeEach
    void setup() {
        User savedUser = RandomModelUtil.randomUser();
        String password = savedUser.getPassword();
        savedUser.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(savedUser).block();
        assert user != null;
        user.setPassword(password);
    }

    @Test
    void testLoginWithValidRequest_shouldReturnValidToken() {
        webClient.post()
                .uri("api/v1/auth/login")
                .bodyValue(new AuthenticationRequest(user.getUsername(), user.getPassword()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody(Map.class)
                .value(tokenMap -> {
                    final String token = (String) tokenMap.get("token");
                    assertThat(token).isNotNull();
                });
    }

    @Test
    void testLoginWithIncorrectPassword_shouldReturn401() {
        webClient.post()
                .uri("api/v1/auth/login")
                .bodyValue(new AuthenticationRequest(user.getUsername(), RandomModelUtil.randomString(10)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testLoginWithNotExistingUser_shouldReturn401() {
        User notExistingUser = RandomModelUtil.randomUser();

        webClient.post()
                .uri("api/v1/auth/login")
                .bodyValue(new AuthenticationRequest(notExistingUser.getUsername(), notExistingUser.getUsername()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testVerifyTokenWithValidToken_shouldReturnUser() {
        String validToken = TestJwtUtil.generateTestToken(user, jwtSigningKey);

        webClient.get()
                .uri("api/v1/auth/verify/{id}", validToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(verifiedUser -> {
                    assertThat(verifiedUser.getId()).isEqualTo(user.getId());
                    assertThat(verifiedUser.getUsername()).isEqualTo(user.getUsername());
                    assertThat(verifiedUser.getCreatedAt()).isEqualTo(user.getCreatedAt().getValue());
                    assertThat(verifiedUser.getRole()).isEqualTo(user.getRole().name());
                });
    }

    @Test
    void testVerifyExpiredToken_shouldReturn403() {
        String validToken = TestJwtUtil.generateTestToken(user, -1, jwtSigningKey);

        webClient.get()
                .uri("api/v1/auth/verify/{id}", validToken)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @TestConfiguration
    static class AuthControllerIntegrationTestConfig {
        @Bean
        @ServiceConnection
        MongoDBContainer mongoDbContainer() {
            return new MongoDBContainer("mongo:latest");
        }

    }


}