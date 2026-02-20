package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;

public record CreateUserResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String birthDate
) {
    public static CreateUserResponse from(User user) {
        return new CreateUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate()
        );
    }
}
