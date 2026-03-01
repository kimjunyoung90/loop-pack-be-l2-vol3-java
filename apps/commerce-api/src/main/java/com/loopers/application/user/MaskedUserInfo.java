package com.loopers.application.user;

import com.loopers.domain.user.User;

public record MaskedUserInfo(
        Long id,
        String loginId,
        String name,
        String birthDate,
        String email
) {
    public static MaskedUserInfo from(User user) {
        return new MaskedUserInfo(
                user.getId(),
                user.getLoginId(),
                user.getMaskedName(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
