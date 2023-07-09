package com.wishquill.userservice.controllers;

import com.wishquill.userservice.dto.AuthenticationRequest;
import com.wishquill.userservice.dto.UserDto;
import com.wishquill.userservice.mappers.UserMapper;
import com.wishquill.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody @Validated AuthenticationRequest request) {
        return authService.loginUser(request)
                .map(this::createLoginResponse);
    }

    @GetMapping(value = "/verify/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    private Mono<ResponseEntity<UserDto>> verifyToken(@PathVariable String token) {
        return authService.verifyToken(token)
                .map(UserMapper::of)
                .map(ResponseEntity::ok);
    }

    private ResponseEntity<Map<String, String>> createLoginResponse(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return new ResponseEntity<>(Map.of("token", token), headers, HttpStatus.OK);
    }

}
