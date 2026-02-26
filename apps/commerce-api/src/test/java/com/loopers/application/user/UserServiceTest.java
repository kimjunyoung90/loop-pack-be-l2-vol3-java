package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.domain.user.UserRepository;
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
    void 사용자_정보_조회시_ID와_일치한_사용자_정보가_없는_경우_AuthenticationFailedException_예외가_발생한다() {
        // given
        String loginId = "rlawnsdud05";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(loginId, "password123!"))
                .isInstanceOf(CoreException.class);
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
        given(passwordEncoder.matches(rawPassword, "encodedPassword")).willReturn(true);

        // when
        UserInfo userInfo = userService.getMyInfo(loginId, rawPassword);

        // then
        assertThat(userInfo, not(hasProperty("password")));
    }

    @Test
    void 사용자_정보_조회시_비밀번호가_일치하지_않으면_AuthenticationFailedException이_발생한다() {
        // given
        String loginId = "loginId";
        String rawPassword = "validPass1!";
        String wrongPassword = "wrongPass1!";
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
        given(passwordEncoder.matches(wrongPassword, "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(loginId, wrongPassword))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 올바른_인증_정보로_인증하면_User를_반환한다() {
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
        given(passwordEncoder.matches(rawPassword, "encodedPassword")).willReturn(true);

        // when
        User result = userService.authenticateUser(loginId, rawPassword);

        // then
        assertThat(result.getLoginId(), is(loginId));
    }

    @Test
    void 존재하지_않는_loginId로_인증하면_CoreException_UNAUTHORIZED가_발생한다() {
        // given
        given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.authenticateUser("unknown", "password1!"))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 잘못된_비밀번호로_인증하면_CoreException_UNAUTHORIZED가_발생한다() {
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
        given(passwordEncoder.matches("wrongPass1!", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.authenticateUser(loginId, "wrongPass1!"))
                .isInstanceOf(CoreException.class);
    }
}
