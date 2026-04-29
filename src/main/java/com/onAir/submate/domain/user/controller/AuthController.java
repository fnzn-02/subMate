package com.onAir.submate.domain.user.controller;

import com.onAir.submate.domain.user.dto.LoginRequest;
import com.onAir.submate.domain.user.dto.LoginResponse;
import com.onAir.submate.domain.user.dto.SignupRequest;
import com.onAir.submate.domain.user.service.AuthService;
import com.onAir.submate.domain.user.service.PasswordResetService;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import com.onAir.submate.global.mail.EmailVerificationStore;
import com.onAir.submate.global.mail.MailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationStore verificationStore;
    private final PasswordResetService passwordResetService;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    // ── 회원가입 / 로그인 ──────────────────────────────
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ── 이메일 인증 코드 발송 (TODO: FCM 푸시 알림으로 교체 예정)
    @PostMapping("/email/send-code")
    public ResponseEntity<Void> sendEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || !email.contains("@")) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        verificationStore.save(email, code);
        mailService.sendVerificationCode(email, code);
        log.info("이메일 인증 코드 발급 - email: {}, code: {}", email, code);
        return ResponseEntity.ok().build();
    }

    // ── 이메일 인증 코드 확인 ─────────────────────────
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code  = body.get("code");
        if (!verificationStore.verify(email, code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        return ResponseEntity.ok().build();
    }

    // ── 이메일(아이디) 찾기 - 닉네임으로 조회 ─────────
    @PostMapping("/find-email")
    public ResponseEntity<Map<String, String>> findEmail(@RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        String maskedEmail = authService.findEmailByNickname(nickname);
        return ResponseEntity.ok(Map.of("maskedEmail", maskedEmail));
    }

    // ── 비밀번호 재설정 코드 발송 ─────────────────────
    @PostMapping("/reset-password/send-code")
    public ResponseEntity<Void> sendResetCode(@RequestBody Map<String, String> body) {
        passwordResetService.sendResetCode(body.get("email"));
        return ResponseEntity.ok().build();
    }

    // ── 비밀번호 재설정 ───────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String code,
            @NotBlank @Size(min = 8) String newPassword
    ) {}
}
