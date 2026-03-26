package com.onAir.submate.domain.user.service;

import com.onAir.submate.domain.user.dto.LoginRequest;
import com.onAir.submate.domain.user.dto.LoginResponse;
import com.onAir.submate.domain.user.dto.SignupRequest;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import com.onAir.submate.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateToken(user.getId(), user.getEmail());
        return LoginResponse.of(token, user.getId(), user.getNickname(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public String findEmailByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String email = user.getEmail();
        int atIdx = email.indexOf('@');
        String local = email.substring(0, atIdx);
        String masked = local.substring(0, Math.min(2, local.length())) + "***" + email.substring(atIdx);
        return masked;
    }
}
