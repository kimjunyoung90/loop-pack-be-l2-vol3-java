package com.loopers.application.user.command;

public record CreateUserCommand(
        String loginId,
        String password,
        String name,
        String birthDate,
        String email
) {
}
