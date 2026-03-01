package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserCommand;
import com.loopers.application.user.MaskedUserInfo;
import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserService userService;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.CreateUserResponse> createUser(@Valid @RequestBody UserV1Dto.CreateUserRequest request) {
        UserCommand.Create command = new UserCommand.Create(
                request.loginId(), request.password(), request.name(), request.birthDate(), request.email()
        );
        UserInfo userInfo = userService.createUser(command);
        return ApiResponse.success(UserV1Dto.CreateUserResponse.from(userInfo));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.GetMyInfoResponse> getMyInfo(
            @LoginUser AuthUser authUser
    ) {
        MaskedUserInfo userInfo = userService.getMyInfo(authUser.loginId());
        return ApiResponse.success(UserV1Dto.GetMyInfoResponse.from(userInfo));
    }

    @PatchMapping("/password")
    @Override
    public ApiResponse<Object> changePassword(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody UserV1Dto.ChangePasswordRequest request
    ) {
        userService.changePassword(authUser.loginId(), request.newPassword());
        return ApiResponse.success();
    }
}
