package com.wishquill.userservice.service;

import com.wishquill.userservice.dto.AuthenticationRequest;
import com.wishquill.userservice.models.User;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<String> loginUser(AuthenticationRequest authRequestMono);

    Mono<User> verifyToken(String tokenToVerify);
}
