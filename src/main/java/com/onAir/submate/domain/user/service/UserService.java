package com.onAir.submate.domain.user.service;

import com.onAir.submate.domain.notification.repository.NotificationLogRepository;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.dto.NicknameUpdateRequest;
import com.onAir.submate.domain.user.dto.PasswordUpdateRequest;
import com.onAir.submate.domain.user.dto.ProfileUpdateRequest;
import com.onAir.submate.domain.user.dto.UserResponse;
import com.onAir.submate.domain.user.entity.ThemeMode;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import com.onAir.submate.global.infra.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = getUser(userId);
        user.updateNickname(request.nickname());
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserResponse uploadProfileImage(Long userId, MultipartFile file) {
        User user = getUser(userId);

        // 기존 이미지 삭제
        fileStorageService.delete(user.getProfileImageUrl());

        String imageUrl = fileStorageService.storeProfileImage(file);
        user.updateProfileImageUrl(imageUrl);

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUser(userId);
        user.updateProfile(request.job(), request.hobby());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateTheme(Long userId, ThemeMode themeMode) {
        User user = getUser(userId);
        user.updateThemeMode(themeMode);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateFcmToken(Long userId, String token) {
        getUser(userId).updateFcmToken(token);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = getUser(userId);

        // 알림 로그 삭제 (FK 제약 때문에 구독 삭제 전에)
        notificationLogRepository.deleteByUserId(userId);

        // 구독 데이터 일괄 삭제
        subscriptionRepository.deleteAllByUser(user);

        // 프로필 이미지 삭제
        fileStorageService.delete(user.getProfileImageUrl());

        userRepository.delete(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
