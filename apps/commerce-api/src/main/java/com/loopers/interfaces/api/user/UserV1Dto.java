package com.loopers.interfaces.api.user;

import com.loopers.application.user.MaskedUserInfo;
import com.loopers.application.user.UserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserV1Dto {

    public record CreateUserRequest(
            @NotBlank
            String loginId,
            @NotBlank
            @Size(min = 8, max = 16)
            @Pattern(regexp = "^[a-zA-Z\\d\\p{Punct}]+$")
            String password,
            @NotBlank
            String name,
            @NotBlank
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
            String birthDate,
            @Email
            @NotBlank
            String email
    ) {
    }

    public record CreateUserResponse(
            Long id,
            String loginId,
            String name,
            String email,
            String birthDate
    ) {
        public static CreateUserResponse from(UserInfo userInfo) {
            return new CreateUserResponse(
                    userInfo.id(),
                    userInfo.loginId(),
                    userInfo.name(),
                    userInfo.email(),
                    userInfo.birthDate()
            );
        }
    }

    public record GetMyInfoResponse(
            String loginId,
            String name,
            String birthDate,
            String email
    ) {
        public static GetMyInfoResponse from(MaskedUserInfo userInfo) {
            return new GetMyInfoResponse(
                    userInfo.loginId(),
                    userInfo.name(),
                    userInfo.birthDate(),
                    userInfo.email()
            );
        }
    }

    public record ChangePasswordRequest(
            @NotBlank
            @Size(min = 8, max = 16)
            @Pattern(regexp = "^[a-zA-Z\\d\\p{Punct}]+$")
            String newPassword
    ) {
    }
}
