package com.wishquill.userservice.service;

import com.wishquill.userservice.dto.CreateUserRequest;
import com.wishquill.userservice.exceptions.UserAlreadyExistsException;
import com.wishquill.userservice.exceptions.UserNotFoundException;
import com.wishquill.userservice.models.User;
import com.wishquill.userservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.BsonTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> getUserById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Not found user with id <%s>", id)));
    }

    @Override
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Not found user with username <%s>", username)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<Void> createUser(CreateUserRequest request) {
        return userRepository.existsByUsername(request.getUsername())
                .handle((isExist, sink) -> {
                    if (isExist)
                        sink.error(new UserAlreadyExistsException("User already exists with username <%s>", request.getUsername()));
                    else {
                        User userToCreate = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole())
                                .createdAt(new BsonTimestamp(System.currentTimeMillis()))
                                .build();
                        sink.next(userToCreate);
                    }
                })
                .cast(User.class)
                .flatMap(userRepository::save)
                .then();
    }
}
