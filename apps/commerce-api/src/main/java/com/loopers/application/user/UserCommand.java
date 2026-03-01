package com.loopers.application.user;

public class UserCommand {

    public record Create(
            String loginId,
            String password,
            String name,
            String birthDate,
            String email
    ) {
    }
}
