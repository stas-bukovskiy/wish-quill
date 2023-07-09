package com.wishquill.userservice.controllers;

import com.wishquill.userservice.dto.CreateUserRequest;
import com.wishquill.userservice.dto.UserDto;
import com.wishquill.userservice.models.User;
import com.wishquill.userservice.models.UserRole;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

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
        savedUser.setRole(UserRole.ADMIN);
        savedUser.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(savedUser).block();
        assert user != null;
        user.setPassword(password);
    }

    @Test
    void testGetUserByIdWithValidId_shouldReturnUser() {
        webClient.get()
                .uri("api/v1/users/{id}", user.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateTestToken(user, jwtSigningKey))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(foundUser -> {
                    assertThat(foundUser.getId()).isEqualTo(user.getId());
                    assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
                    assertThat(foundUser.getRole()).isEqualTo(user.getRole().name());
                    assertThat(foundUser.getCreatedAt()).isEqualTo(user.getCreatedAt().getValue());
                });
    }

    @Test
    void testGetUserByIdWithInvalidId_shouldReturn404() {
        webClient.get()
                .uri("api/v1/users/{id}", UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateTestToken(user, jwtSigningKey))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetUserByIdWithoutToken_shouldReturn401() {
        webClient.get()
                .uri("api/v1/users/{id}", UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateUserWithInvalidRequest_shouldReturn400() {
        CreateUserRequest request = RandomModelUtil.randomCreateUserRequest();
        request.setUsername(request.getUsername().substring(0, 2));

        webClient.post()
                .uri("api/v1/users/register")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        Mono<User> foundUser = userRepository.findByUsername(request.getUsername());

        StepVerifier.create(foundUser)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testCreateUserWithValidRequest_shouldCreateUser() {
        CreateUserRequest request = RandomModelUtil.randomCreateUserRequest();

        webClient.post()
                .uri("api/v1/users/register")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
        Mono<User> createdUser = userRepository.findByUsername(request.getUsername());

        StepVerifier.create(createdUser)
                .consumeNextWith(actualUser -> {
                    assertThat(actualUser.getId()).isNotNull();
                    assertThat(actualUser.getUsername()).isEqualTo(request.getUsername());
                    assertThat(actualUser.getPassword()).satisfies(password -> passwordEncoder.matches(request.getPassword(), password));
                    assertThat(actualUser.getRole()).isEqualTo(UserRole.USER);
                    assertThat(actualUser.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testCreateAdminWithValidRequest_shouldCreateUser() {
        CreateUserRequest request = RandomModelUtil.randomCreateUserRequest();

        webClient.post()
                .uri("api/v1/users/register/admin")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateTestToken(user, jwtSigningKey))
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
        Mono<User> createdUser = userRepository.findByUsername(request.getUsername());

        StepVerifier.create(createdUser)
                .consumeNextWith(actualUser -> {
                    assertThat(actualUser.getId()).isNotNull();
                    assertThat(actualUser.getUsername()).isEqualTo(request.getUsername());
                    assertThat(actualUser.getPassword()).satisfies(password -> passwordEncoder.matches(request.getPassword(), password));
                    assertThat(actualUser.getRole()).isEqualTo(UserRole.ADMIN);
                    assertThat(actualUser.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testCreateAdminWithoutToken_shouldReturn401() {
        CreateUserRequest request = RandomModelUtil.randomCreateUserRequest();

        webClient.post()
                .uri("api/v1/users/register/admin")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateAdminWithInvalidRequest_shouldReturn400() {
        CreateUserRequest invalidRequest = RandomModelUtil.randomCreateUserRequest();
        invalidRequest.setPassword(invalidRequest.getPassword().substring(0, 5));

        webClient.post()
                .uri("api/v1/users/register/admin")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateTestToken(user, jwtSigningKey))
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @TestConfiguration
    static class UserControllerIntegrationTestConfig {
        @Bean
        @ServiceConnection
        public MongoDBContainer mongoDBContainer() {
            return new MongoDBContainer("mongo:latest");
        }

    }
}