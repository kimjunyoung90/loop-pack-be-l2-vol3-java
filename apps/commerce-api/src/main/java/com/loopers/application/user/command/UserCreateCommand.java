package com.loopers.application.user.command;

public record UserCreateCommand(
        String loginId,
        String password,
        String name,
        String birthDate,
        String email
) {
}
