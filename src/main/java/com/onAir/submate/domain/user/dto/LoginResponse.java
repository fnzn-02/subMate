package com.onAir.submate.domain.user.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String nickname,
        String email
) {
    public static LoginResponse of(String token, Long userId, String nickname, String email) {
        return new LoginResponse(token, "Bearer", userId, nickname, email);
    }
}
