package com.loopers.user.service;

import com.loopers.user.domain.User;
import com.loopers.user.dto.CreateUserRequest;
import com.loopers.user.dto.GetMyInfoResponse;
import com.loopers.user.exception.DuplicateLoginIdException;
import com.loopers.user.exception.SamePasswordException;
import com.loopers.user.exception.UserNotFoundException;
import com.loopers.user.repository.UserRepository;
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
    public User createUser(CreateUserRequest request) {

        if(userRepository.existsByLoginId(request.loginId())){
            throw new DuplicateLoginIdException();
        }

        User user = new User(
                request.loginId(),
                request.password(),
                request.name(),
                request.birthDate(),
                request.email(),
                passwordEncoder
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public GetMyInfoResponse getMyInfo(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(UserNotFoundException::new);

        return GetMyInfoResponse.from(user);
    }

    @Transactional
    public void changePassword(String loginId, String currentPassword, String newPassword) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(UserNotFoundException::new);

        // 새 비밀번호가 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException();
        }

        // 비밀번호 암호화 후 저장
        user.setPassword(newPassword, user.getBirthDate(), passwordEncoder);
    }
}
