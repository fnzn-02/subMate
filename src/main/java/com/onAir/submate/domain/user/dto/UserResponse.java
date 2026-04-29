package com.onAir.submate.domain.user.dto;

import com.onAir.submate.domain.user.entity.ThemeMode;
import com.onAir.submate.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        ThemeMode themeMode,
        String job,
        String hobby,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getThemeMode(),
                user.getJob(),
                user.getHobby(),
                user.getCreatedAt()
        );
    }
}
