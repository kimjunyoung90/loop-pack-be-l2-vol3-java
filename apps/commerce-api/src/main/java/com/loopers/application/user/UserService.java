package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserInfo createUser(CreateUserCommand command) {

        if(userRepository.existsByLoginId(command.loginId())){
            throw new CoreException(ErrorType.CONFLICT, "이미 사용 중인 로그인 ID입니다.");
        }

        User user = User.builder()
                .loginId(command.loginId())
                .password(command.password())
                .name(command.name())
                .birthDate(command.birthDate())
                .email(command.email())
                .passwordEncoder(passwordEncoder)
                .build();

        return UserInfo.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserInfo getMyInfo(String loginId, String password) {
        authenticate(loginId, password);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return UserInfo.fromWithMaskedName(user);
    }

    @Transactional
    public void changePassword(String loginId, String password, String newPassword) {
        authenticate(loginId, password);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 새 비밀번호가 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 비밀번호 암호화 후 저장
        user.setPassword(newPassword, user.getBirthDate(), passwordEncoder);
    }

    private void authenticate(String loginId, String password) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "인증에 실패했습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "인증에 실패했습니다.");
        }
    }
}
