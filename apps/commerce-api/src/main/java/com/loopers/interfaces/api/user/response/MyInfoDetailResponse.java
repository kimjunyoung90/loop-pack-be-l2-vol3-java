package com.loopers.interfaces.api.user.response;

import com.loopers.application.user.result.UserResult;

public record MyInfoDetailResponse(
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static MyInfoDetailResponse from(UserResult userResult) {
        return new MyInfoDetailResponse(
                userResult.loginId(),
                userResult.name(),
                userResult.birthDate(),
                userResult.email()
        );
    }
}
