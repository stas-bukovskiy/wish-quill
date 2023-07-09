package com.wishquill.userservice.service;

import com.wishquill.userservice.dto.CreateUserRequest;
import com.wishquill.userservice.models.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> getUserById(String id);

    Mono<User> getUserByUsername(String username);

    Mono<Void> createUser(CreateUserRequest request);
}
