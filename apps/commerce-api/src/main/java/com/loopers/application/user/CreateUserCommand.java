package com.loopers.application.user;

public record CreateUserCommand(
        String loginId,
        String password,
        String name,
        String birthDate,
        String email
) {
}
