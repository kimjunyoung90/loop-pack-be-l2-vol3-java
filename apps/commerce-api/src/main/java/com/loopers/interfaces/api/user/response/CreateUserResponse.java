package com.loopers.interfaces.api.user.response;

import com.loopers.application.user.result.UserResult;

public record CreateUserResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String birthDate
) {
    public static CreateUserResponse from(UserResult userResult) {
        return new CreateUserResponse(
                userResult.id(),
                userResult.loginId(),
                userResult.name(),
                userResult.email(),
                userResult.birthDate()
        );
    }
}
