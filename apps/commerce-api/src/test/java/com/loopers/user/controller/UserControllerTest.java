package com.loopers.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.user.dto.CreateUserRequest;
import com.loopers.user.service.UserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
//@Import(GlobalExceptionHandler.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @ParameterizedTest(name = "{1} 누락")
    @MethodSource("필수값_누락_케이스")
    void 회원가입시_필수값이_누락되면_응답코드_400을_반환한다(CreateUserRequest request, String nullField) throws Exception {

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
                Arguments.of(new CreateUserRequest(null, "pw", "name", "1990-01-01", "a@a.com"), "loginId"),
                Arguments.of(new CreateUserRequest("test", null, "name", "1990-01-01", "a@a.com"), "password"),
                Arguments.of(new CreateUserRequest("test", "pw", null, "1990-01-01", "a@a.com"), "name"),
                Arguments.of(new CreateUserRequest("test", "pw", "name", null, "a@a.com"), "birthDate"),
                Arguments.of(new CreateUserRequest("test", "pw", "name", "1990-01-01", null), "email")
        );
    }

    @Test
    void 이메일_형식이_오류시_응답코드_400을_반환한다() throws Exception {
        //given
        String email = "testtest.com";
        CreateUserRequest request = new CreateUserRequest(
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
    void 생년월일_형식_오류_시_400_Bad_Request_반환() throws Exception {
        //given
        CreateUserRequest request = new CreateUserRequest(
                "testId", "password123!", "김준영", "1990-0427", "test@test.com"
        );

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpect(status().isBadRequest());
    }
}
