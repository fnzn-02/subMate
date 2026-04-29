package com.onAir.submate.domain.user.service;

import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import com.onAir.submate.global.mail.EmailVerificationStore;
import com.onAir.submate.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationStore store;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    public void sendResetCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        String code = String.format("%06d", random.nextInt(1_000_000));
        store.save("RESET:" + email, code);
        mailService.sendPasswordResetCode(email, code);
        log.info("비밀번호 재설정 코드 발급 - email: {}, code: {}", email, code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        if (!store.verify("RESET:" + email, code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(newPassword));
        store.remove("RESET:" + email);
    }
}
