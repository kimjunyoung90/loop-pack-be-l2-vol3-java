package com.loopers.interfaces.api.user.response;

import com.loopers.application.user.result.UserResult;

public record GetMyInfoResponse(
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static GetMyInfoResponse from(UserResult userResult) {
        return new GetMyInfoResponse(
                userResult.loginId(),
                userResult.name(),
                userResult.birthDate(),
                userResult.email()
        );
    }
}
