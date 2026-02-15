package com.loopers.user.controller;

import com.loopers.user.dto.CreateUserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void 이메일_형식_불일치_시_실패() {
        //given
        String id = "test";
        String password = "pw";
        String name = "name";
        String birthDate = "1990-01-01";
        String email = "test123";
        CreateUserRequest request = new CreateUserRequest(id, password, name, birthDate, email);

        //when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        //then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateUserRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getConstraintDescriptor()
                .getAnnotation()
                .annotationType())
                .isEqualTo(Email.class);
    }

    @Test
    void 생년월일_형식_불일치_시_실패() {
        //given
        String id = "test";
        String password = "pw";
        String name = "name";
        String birthDate = "19900427";
        String email = "test123@test.com";
        CreateUserRequest request = new CreateUserRequest(id, password, name, birthDate, email);

        //when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        //then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateUserRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("birthDate");
    }

    @Test
    void 로그인ID에_영문_숫자_외_문자_포함_시_실패() {
        //given
        String id = "test@123";
        String password = "pw";
        String name = "name";
        String birthDate = "1990-01-01";
        String email = "test@test.com";
        CreateUserRequest request = new CreateUserRequest(id, password, name, birthDate, email);

        //when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        //then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateUserRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("loginId");
        assertThat(violation.getConstraintDescriptor()
                .getAnnotation()
                .annotationType())
                .isEqualTo(Pattern.class);
    }
}
