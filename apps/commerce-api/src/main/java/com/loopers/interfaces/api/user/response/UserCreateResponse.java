package com.loopers.interfaces.api.user.response;

import com.loopers.application.user.result.UserResult;

public record UserCreateResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String birthDate
) {
    public static UserCreateResponse from(UserResult userResult) {
        return new UserCreateResponse(
                userResult.id(),
                userResult.loginId(),
                userResult.name(),
                userResult.email(),
                userResult.birthDate()
        );
    }
}
