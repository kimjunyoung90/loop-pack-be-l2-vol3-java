package com.loopers.interfaces.api.user.response;

import com.loopers.application.user.result.UserResult;

public record MyInfoGetResponse(
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static MyInfoGetResponse from(UserResult userResult) {
        return new MyInfoGetResponse(
                userResult.loginId(),
                userResult.name(),
                userResult.birthDate(),
                userResult.email()
        );
    }
}
