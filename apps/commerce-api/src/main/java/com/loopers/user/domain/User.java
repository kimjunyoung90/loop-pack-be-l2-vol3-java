package com.loopers.user.domain;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
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
    public User(String loginId, String password, String name, String birthDate, String email) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
    }

    public String getMaskedName() {
        if (name.length() == 1) {
            return "*";
        }
        return name.substring(0, name.length() - 1) + "*";
    }

    public void setPassword(String password, String birthDate, PasswordEncoder passwordEncoder) {
        if (password.length() < 8 || password.length() > 16) {
            throw new IllegalArgumentException();
        }
        if(!password.matches("^[a-zA-Z\\d\\p{Punct}]+$")) {
            throw new IllegalArgumentException();
        }
        if(password.contains(birthDate)) {
            throw new IllegalArgumentException("비밀번호는 생년월일을 포함할 수 없습니다.");
        }

        this.password = passwordEncoder.encode(password);
    }

    public void setEmail(String email) {
        if(!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException();
        }
    }

    public void setBirthDate(String birthDate) {
        try {
            LocalDate.parse(birthDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException();
        }
    }
}
