package com.wishquill.userservice.repositories;

import com.wishquill.userservice.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);
}
