package com.loopers.application.user.result;

import com.loopers.domain.user.User;

public record UserResult(
        Long id,
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getBirthDate(),
                user.getEmail()
        );
    }

    public static UserResult fromWithMaskedName(User user) {
        return new UserResult(
                user.getId(),
                user.getLoginId(),
                user.getMaskedName(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
