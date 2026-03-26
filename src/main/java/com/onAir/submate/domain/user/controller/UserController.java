package com.onAir.submate.domain.user.controller;

import com.onAir.submate.domain.user.dto.NicknameUpdateRequest;
import com.onAir.submate.domain.user.dto.PasswordUpdateRequest;
import com.onAir.submate.domain.user.dto.UserResponse;
import com.onAir.submate.domain.user.entity.ThemeMode;
import com.onAir.submate.domain.user.service.UserService;
import com.onAir.submate.global.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getMe() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PutMapping("/nickname")
    public ResponseEntity<UserResponse> updateNickname(@Valid @RequestBody NicknameUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateNickname(userId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadProfileImage(
            @RequestPart("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.uploadProfileImage(userId, file));
    }

    @PutMapping("/theme")
    public ResponseEntity<UserResponse> updateTheme(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        ThemeMode themeMode = ThemeMode.valueOf(body.getOrDefault("themeMode", "LIGHT").toUpperCase());
        return ResponseEntity.ok(userService.updateTheme(userId, themeMode));
    }

    @DeleteMapping
    public ResponseEntity<Void> withdraw() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
