package com.loopers.application.user;

import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 가입된_ID로_회원가입시_DuplicateLoginIdException이_발생한다() {
        //given
        String loginId = "testuser";
        UserCommand.Create command = new UserCommand.Create(
                loginId, "password123!", "홍길동", "1990-04-27", "test@test.com"
        );
        userService.createUser(command);

        //when
        //동일한 아이디로 가입
        UserCommand.Create duplicateCommand = new UserCommand.Create(
                loginId, "password456!", "김철수", "1995-01-01", "other@test.com"
        );
        Throwable thrown = catchThrowable(() -> userService.createUser(duplicateCommand));

        //then
        assertThat(thrown).isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_ID로_회원가입시_회원가입에_성공한다() {
        //given
        UserCommand.Create command = new UserCommand.Create(
                "testuser", "password123!", "홍길동", "1990-04-27", "test@test.com"
        );

        //when
        UserInfo userInfo = userService.createUser(command);

        //then
        User foundUser = userRepository.findByLoginId(userInfo.loginId()).orElseThrow();
        assertThat(foundUser.getLoginId()).isEqualTo(command.loginId());
        assertThat(foundUser.getName()).isEqualTo(command.name());
        assertThat(foundUser.getEmail()).isEqualTo(command.email());
    }

    @Test
    void DB에_저장된_사용자의_비밀번호가_정상적으로_변경된다() {
        // given
        String loginId = "testuser";
        String currentPassword = "password123!";
        String newPassword = "newPassword456!";

        UserCommand.Create command = new UserCommand.Create(
                loginId, currentPassword, "홍길동", "1990-01-01", "test@test.com"
        );
        userService.createUser(command);

        // when
        userService.changePassword(loginId, newPassword);

        // then
        User updatedUser = userRepository.findByLoginId(loginId).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    void 비밀번호_변경시_새_비밀번호가_기존과_동일하면_SamePasswordException이_발생한다() {
        // given
        String loginId = "testuser";
        String currentPassword = "password123!";
        String newPassword = "password123!";

        UserCommand.Create command = new UserCommand.Create(
                loginId, currentPassword, "홍길동", "1990-01-01", "test@test.com"
        );
        userService.createUser(command);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(loginId, newPassword))
                .isInstanceOf(CoreException.class);
    }
}
