package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, length = 10, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String birthDate;

    @Column(nullable = false)
    private String email;

    @Builder
    private User(String loginId, String password, String name, String birthDate, String email, PasswordEncoder passwordEncoder) {
        if (password.length() < 8 || password.length() > 16) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 8자 이상 16자 이하여야 합니다.");
        }
        if (!password.matches("^[a-zA-Z\\d\\p{Punct}]+$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.");
        }
        if (password.contains(birthDate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 생년월일을 포함할 수 없습니다.");
        }
        this.loginId = loginId;
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
        this.password = passwordEncoder.encode(password);
        guard();
    }

    public String getMaskedName() {
        return name.substring(0, name.length() - 1) + "*";
    }

    public void changePassword(String password, String birthDate, PasswordEncoder passwordEncoder) {
        if (password.length() < 8 || password.length() > 16) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 8자 이상 16자 이하여야 합니다.");
        }
        if (!password.matches("^[a-zA-Z\\d\\p{Punct}]+$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.");
        }
        if (password.contains(birthDate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 생년월일을 포함할 수 없습니다.");
        }
        this.password = passwordEncoder.encode(password);
    }

    public void changeEmail(String email) {
        this.email = email;
        guard();
    }

    public void changeBirthDate(String birthDate) {
        this.birthDate = birthDate;
        guard();
    }

    @Override
    protected void guard() {
        if (loginId == null || loginId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "로그인 ID는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름은 필수입니다.");
        }
        try {
            LocalDate.parse(birthDate);
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "올바른 생년월일 형식이 아닙니다.");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "올바른 이메일 형식이 아닙니다.");
        }
    }
}
