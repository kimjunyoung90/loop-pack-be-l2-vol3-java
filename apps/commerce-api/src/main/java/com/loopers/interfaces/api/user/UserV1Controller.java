package com.loopers.interfaces.api.user;

import com.loopers.application.user.command.CreateUserCommand;
import com.loopers.application.user.result.UserResult;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.request.ChangePasswordRequest;
import com.loopers.interfaces.api.user.request.CreateUserRequest;
import com.loopers.interfaces.api.user.response.CreateUserResponse;
import com.loopers.interfaces.api.user.response.GetMyInfoResponse;
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
    public ApiResponse<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.loginId(), request.password(), request.name(), request.birthDate(), request.email()
        );
        UserResult userResult = userService.createUser(command);
        return ApiResponse.success(CreateUserResponse.from(userResult));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<GetMyInfoResponse> getMyInfo(
            @LoginUser AuthUser authUser
    ) {
        UserResult userResult = userService.getMyInfo(authUser.loginId());
        return ApiResponse.success(GetMyInfoResponse.from(userResult));
    }

    @PatchMapping("/password")
    @Override
    public ApiResponse<Object> changePassword(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authUser.loginId(), request.newPassword());
        return ApiResponse.success();
    }
}
