package com.loopers.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserV1Controller.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @ParameterizedTest(name = "{1} 누락")
    @MethodSource("필수값_누락_케이스")
    void 회원가입시_필수값이_누락되면_응답코드_400을_반환한다(UserV1Dto.CreateUserRequest request, String nullField) throws Exception {

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    static Stream<Arguments> 필수값_누락_케이스() {
        return Stream.of(
                Arguments.of(new UserV1Dto.CreateUserRequest(null, "pw", "name", "1990-01-01", "a@a.com"), "loginId"),
                Arguments.of(new UserV1Dto.CreateUserRequest("test", null, "name", "1990-01-01", "a@a.com"), "password"),
                Arguments.of(new UserV1Dto.CreateUserRequest("test", "pw", null, "1990-01-01", "a@a.com"), "name"),
                Arguments.of(new UserV1Dto.CreateUserRequest("test", "pw", "name", null, "a@a.com"), "birthDate"),
                Arguments.of(new UserV1Dto.CreateUserRequest("test", "pw", "name", "1990-01-01", null), "email")
        );
    }

    @Test
    void 이메일_형식_오류시_응답코드_400을_반환한다() throws Exception {
        //given
        String email = "testtest.com";
        UserV1Dto.CreateUserRequest request = new UserV1Dto.CreateUserRequest(
                "testId", "password123!", "김준영", "1990-04-27", email
        );

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 이메일_도메인_누락시_응답코드_400을_반환한다() throws Exception {
        //given
        String email = "test@";
        UserV1Dto.CreateUserRequest request = new UserV1Dto.CreateUserRequest(
                "testId", "password123!", "김준영", "1990-04-27", email
        );

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 생년월일_형식_오류시_응답코드_400을_반환한다() throws Exception {
        //given
        String birthDate = "1990-0427";
        UserV1Dto.CreateUserRequest request = new UserV1Dto.CreateUserRequest(
                "testId", "password123!", "김준영", birthDate, "test@test.com"
        );

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "비밀번호가 \"{0}\"이면 400")
    @MethodSource("회원가입_비밀번호_형식_오류_케이스")
    void 회원가입시_비밀번호_형식이_올바르지_않으면_응답코드_400을_반환한다(String password) throws Exception {
        UserV1Dto.CreateUserRequest request = new UserV1Dto.CreateUserRequest(
                "testId", password, "김준영", "1990-04-27", "test@test.com"
        );

        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        result.andExpect(status().isBadRequest());
    }

    static Stream<Arguments> 회원가입_비밀번호_형식_오류_케이스() {
        return Stream.of(
                Arguments.of("Short1!"),        // 7자 (8자 미만)
                Arguments.of("thisIsWayTooLong1!"), // 17자 (16자 초과)
                Arguments.of("pass한글word1!")    // 허용되지 않은 문자 포함
        );
    }

    @Test
    void 내정보조회시_헤더의_로그인ID와_비밀번호를_서비스에_전달한다() throws Exception {
        //given
        String loginId = "rlawnsdud05";
        String password = "password123!";

        //when
        mockMvc.perform(get("/api/v1/users/me")
                .header("X-Loopers-LoginId", loginId)
                .header("X-Loopers-LoginPw", password)
        );

        //then
        verify(userService).getMyInfo(loginId, password);
    }

    @Test
    void 비밀번호_변경시_신규_비밀번호가_누락되면_400을_반환한다() throws Exception {
        UserV1Dto.ChangePasswordRequest request = new UserV1Dto.ChangePasswordRequest("");

        ResultActions result = mockMvc.perform(patch("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "새 비밀번호가 \"{0}\"이면 400")
    @MethodSource("비밀번호_변경_형식_오류_케이스")
    void 비밀번호_변경시_새_비밀번호_형식이_올바르지_않으면_400을_반환한다(String newPassword) throws Exception {
        UserV1Dto.ChangePasswordRequest request = new UserV1Dto.ChangePasswordRequest(newPassword);

        ResultActions result = mockMvc.perform(patch("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Loopers-LoginId", "testuser01")
                .header("X-Loopers-LoginPw", "password123!")
        );

        result.andExpect(status().isBadRequest());
    }

    static Stream<Arguments> 비밀번호_변경_형식_오류_케이스() {
        return Stream.of(
                Arguments.of("Short1!"),        // 7자 (8자 미만)
                Arguments.of("thisIsWayTooLong1!"), // 17자 (16자 초과)
                Arguments.of("pass한글word1!")    // 허용되지 않은 문자 포함
        );
    }

    @Test
    void 비밀번호_변경시_헤더의_로그인ID와_비밀번호를_서비스에_전달한다() throws Exception {
        String newPassword = "newwpwd123!";
        UserV1Dto.ChangePasswordRequest request = new UserV1Dto.ChangePasswordRequest(newPassword);

        String loginId = "rlawnsdud05";
        String loginPasswd = "password123!";
        mockMvc.perform(patch("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Loopers-LoginId", loginId)
                .header("X-Loopers-LoginPw", loginPasswd)
        );

        verify(userService).changePassword(loginId, loginPasswd, newPassword);
    }
}
