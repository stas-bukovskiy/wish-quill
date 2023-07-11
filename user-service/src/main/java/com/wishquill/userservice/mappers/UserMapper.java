package com.wishquill.userservice.mappers;

import com.wishquill.userservice.dto.UserDto;
import com.wishquill.userservice.models.User;

public final class UserMapper {
    private UserMapper() {

    }

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt().getValue())
                .build();
    }

}
