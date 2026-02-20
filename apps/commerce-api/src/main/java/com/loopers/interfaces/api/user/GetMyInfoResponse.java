package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;

public record GetMyInfoResponse(
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static GetMyInfoResponse from(User user) {
        return new GetMyInfoResponse(
                user.getLoginId(),
                user.getMaskedName(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
