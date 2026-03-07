package com.loopers.interfaces.api.user;

import com.loopers.application.user.command.UserCreateCommand;
import com.loopers.application.user.result.UserResult;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.request.PasswordChangeRequest;
import com.loopers.interfaces.api.user.request.UserCreateRequest;
import com.loopers.interfaces.api.user.response.UserCreateResponse;
import com.loopers.interfaces.api.user.response.MyInfoDetailResponse;
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
    public ApiResponse<UserCreateResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserCreateCommand command = new UserCreateCommand(
                request.loginId(), request.password(), request.name(), request.birthDate(), request.email()
        );
        UserResult userResult = userService.createUser(command);
        return ApiResponse.success(UserCreateResponse.from(userResult));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<MyInfoDetailResponse> getMyInfo(
            @LoginUser AuthUser authUser
    ) {
        UserResult userResult = userService.getMyInfo(authUser.loginId());
        return ApiResponse.success(MyInfoDetailResponse.from(userResult));
    }

    @PatchMapping("/password")
    @Override
    public ApiResponse<Object> changePassword(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(authUser.loginId(), request.newPassword());
        return ApiResponse.success();
    }
}
