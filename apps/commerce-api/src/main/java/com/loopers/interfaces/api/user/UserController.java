package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserService;
import com.loopers.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    public static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    public static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    private final UserService userService;

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateUserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<GetMyInfoResponse> getMyInfo(
            @RequestHeader(LOGIN_ID_HEADER) String loginId,
            @RequestHeader(LOGIN_PW_HEADER) String password
    ) {
        GetMyInfoResponse response = userService.getMyInfo(loginId, password);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(LOGIN_ID_HEADER) String loginId,
            @RequestHeader(LOGIN_PW_HEADER) String password,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(loginId, password, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
