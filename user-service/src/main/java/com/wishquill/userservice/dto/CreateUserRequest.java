package com.wishquill.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wishquill.userservice.models.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateUserRequest {

    @NotBlank(message = "username can not be empty")
    @Size(min = 3, message = "username must be larger that 3 characters")
    private String username;

    @NotBlank(message = "password can not be empty")
    @Size(min = 6, message = "password must be larger that 6 characters")
    private String password;

    @JsonIgnore
    private UserRole role;
}
