package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.request.ChangePasswordRequest;
import com.loopers.interfaces.api.user.request.CreateUserRequest;
import com.loopers.interfaces.api.user.response.CreateUserResponse;
import com.loopers.interfaces.api.user.response.GetMyInfoResponse;
import com.loopers.support.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "사용자 관련 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다."
    )
    ApiResponse<CreateUserResponse> createUser(CreateUserRequest request);

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 정보를 조회합니다."
    )
    ApiResponse<GetMyInfoResponse> getMyInfo(AuthUser authUser);

    @Operation(
        summary = "비밀번호 변경",
        description = "로그인한 사용자의 비밀번호를 변경합니다."
    )
    ApiResponse<Object> changePassword(AuthUser authUser, ChangePasswordRequest request);
}
