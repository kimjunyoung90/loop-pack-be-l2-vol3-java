package com.loopers.user.service;

import com.loopers.user.domain.User;
import com.loopers.user.dto.GetMyInfoResponse;
import com.loopers.user.exception.UserNotFoundException;
import com.loopers.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void 사용자_정보_조회시_ID와_일치한_사용자_정보가_없는_경우_UserNotFoundException_예외가_발생한다() {
        // given
        String loginId = "rlawnsdud05";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(loginId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 사용자_정보_조회시_비밀번호는_반환데이터에서_제외한다() {
        // given
        String loginId = "loginId";
        String rawPassword = "validPass1!";
        given(passwordEncoder.encode(rawPassword)).willReturn("encodedPassword");
        User user = User.builder()
                .loginId(loginId)
                .password(rawPassword)
                .name("홍길동")
                .birthDate("1990-01-01")
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build();
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(user));

        // when
        GetMyInfoResponse response = userService.getMyInfo(loginId);

        // then
        assertThat(response, not(hasProperty("password")));
    }

}
