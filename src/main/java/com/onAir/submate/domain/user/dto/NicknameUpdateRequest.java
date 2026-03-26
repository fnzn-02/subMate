package com.onAir.submate.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
        String nickname
) {}
