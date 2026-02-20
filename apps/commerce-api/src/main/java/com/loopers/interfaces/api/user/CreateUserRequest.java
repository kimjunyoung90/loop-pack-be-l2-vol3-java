package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        String loginId,
        @NotBlank
        @Size(min = 8, max = 16)
        @Pattern(regexp = "^[a-zA-Z\\d\\p{Punct}]+$")
        String password,
        @NotBlank
        String name,
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
        String birthDate,
        @Email
        @NotBlank
        String email
) {
}
