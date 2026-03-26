package com.onAir.submate.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "메시지를 입력해주세요.")
        @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다.")
        String message
) {}
