package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @Size(min = 8, max = 16)
        @Pattern(regexp = "^[a-zA-Z\\d\\p{Punct}]+$")
        String newPassword
) {
}
