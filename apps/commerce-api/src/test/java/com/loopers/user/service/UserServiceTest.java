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

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        String loginId = "loginId";
        given(passwordEncoder.encode("validPass1!")).willReturn("encodedPassword");
        User user = User.builder()
                .loginId(loginId)
                .password("validPass1!")
                .name("홍길동")
                .birthDate("1990-01-01")
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build();
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(user));

        GetMyInfoResponse myInfo = userService.getMyInfo(loginId);

        assertThat(myInfo.getClass().getDeclaredFields())
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("loginId", "name", "birthDate", "email");

    }

}
