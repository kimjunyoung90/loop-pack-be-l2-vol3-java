package com.loopers.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User createValidUser() {
        return User.builder()
                .loginId("testId")
                .password("validPass1!")
                .name("홍길동")
                .birthDate("1990-01-01")
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build();
    }

    @Test
    void 비밀번호가_7자이면_IllegalArgumentException_예외가_발생한다() {
        String password = "1234567";
        User user = createValidUser();

        Throwable thrown = catchThrowable(() -> user.setPassword(password, "1990-01-01", passwordEncoder));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호가_17자이면_IllegalArgumentException_예외가_발생한다() {
        String password = "12345678901234567";
        User user = createValidUser();

        Throwable thrown = catchThrowable(() -> user.setPassword(password, "1990-01-01", passwordEncoder));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호가_8자이면_정상_입력된다() {
        String password = "12345678";
        User user = createValidUser();

        Throwable thrown = catchThrowable(() -> user.setPassword(password, "1990-01-01", passwordEncoder));

        assertThat(thrown).isNull();
    }

    @Test
    void 비밀번호가_16자이면_정상_입력된다() {
        String password = "1234567890123456";
        User user = createValidUser();

        Throwable thrown = catchThrowable(() -> user.setPassword(password, "1990-01-01", passwordEncoder));

        assertThat(thrown).isNull();
    }

    @Test
    void 비밀번호에_한글이_포함되면_IllegalArgumentException이_발생한다() {
        String password = "ㄱ12345561";
        User user = createValidUser();

        Throwable thrown = catchThrowable(() -> user.setPassword(password, "1990-01-01", passwordEncoder));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호에_공백이_포함되면_IllegalArgumentException이_발생한다() {
        String password = "1234 5561";
        User user = createValidUser();

        assertThatThrownBy(() -> user.setPassword(password, "1990-01-01", passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호에_생년월일이_포함되면_IllegalArgumentException_예외가_발생한다() {
        //given
        String password = "1990-01-01!";
        String date = "1990-01-01";
        User user = createValidUser();

        //when
        Throwable thrown = catchThrowable(() -> user.setPassword(password, date, passwordEncoder));

        //then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 사용자_생성시_비밀번호는_암호화되어_저장된다() {
        //given
        String rawPassword = "password123";

        //when
        User user = User.builder()
                .loginId("testId")
                .password(rawPassword)
                .name("홍길동")
                .birthDate("1990-01-01")
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build();

        //then
        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();
    }

    @Test
    void 이메일에_AT이_누락되면_IllegalArgumentException_예외가_발생한다() {
        //given
        String email = "testtest.com";

        //when & then
        assertThatThrownBy(() -> User.builder()
                .loginId("testId")
                .password("validPass1!")
                .name("홍길동")
                .birthDate("1990-01-01")
                .email(email)
                .passwordEncoder(passwordEncoder)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이메일에_도메인이_누락되면_IllegalArgumentException_예외가_발생한다() {
        //given
        String email = "test@";

        //when & then
        assertThatThrownBy(() -> User.builder()
                .loginId("testId")
                .password("validPass1!")
                .name("홍길동")
                .birthDate("1990-01-01")
                .email(email)
                .passwordEncoder(passwordEncoder)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 생년월일이_기준_포맷에_맞지_않으면_IllegalArgumentException_예외가_발생한다() {
        String birthDate = "19900427";

        assertThatThrownBy(() -> User.builder()
                .loginId("testId")
                .password("validPass1!")
                .name("홍길동")
                .birthDate(birthDate)
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 생년월일이_유효한_날짜가_아니면_IllegalArgumentException_예외가_발생한다() {
        String birthDate = "19900231";

        assertThatThrownBy(() -> User.builder()
                .loginId("testId")
                .password("validPass1!")
                .name("홍길동")
                .birthDate(birthDate)
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름_조회시_마지막_글자가_마스킹된다() {
        // given
        User user = User.builder()
                .loginId("testId")
                .password("password123!")
                .name("홍길동")
                .birthDate("1990-01-01")
                .email("test@test.com")
                .passwordEncoder(passwordEncoder)
                .build();

        // when
        String maskedName = user.getMaskedName();

        // then
        assertThat(maskedName).isEqualTo("홍길*");
    }

}
