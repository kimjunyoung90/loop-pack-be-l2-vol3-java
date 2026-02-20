package com.loopers.interfaces.api.user;

import com.loopers.application.user.CreateUserCommand;
import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    public static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    public static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    private final UserService userService;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.CreateUserResponse> createUser(@Valid @RequestBody UserV1Dto.CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.loginId(), request.password(), request.name(), request.birthDate(), request.email()
        );
        UserInfo userInfo = userService.createUser(command);
        return ApiResponse.success(UserV1Dto.CreateUserResponse.from(userInfo));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.GetMyInfoResponse> getMyInfo(
            @RequestHeader(LOGIN_ID_HEADER) String loginId,
            @RequestHeader(LOGIN_PW_HEADER) String password
    ) {
        UserInfo userInfo = userService.getMyInfo(loginId, password);
        return ApiResponse.success(UserV1Dto.GetMyInfoResponse.from(userInfo));
    }

    @PatchMapping("/password")
    @Override
    public ApiResponse<Object> changePassword(
            @RequestHeader(LOGIN_ID_HEADER) String loginId,
            @RequestHeader(LOGIN_PW_HEADER) String password,
            @Valid @RequestBody UserV1Dto.ChangePasswordRequest request
    ) {
        userService.changePassword(loginId, password, request.newPassword());
        return ApiResponse.success();
    }
}
