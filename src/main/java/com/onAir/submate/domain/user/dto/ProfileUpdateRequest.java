package com.onAir.submate.domain.user.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 50, message = "직업은 50자 이하로 입력해주세요.")
        String job,

        @Size(max = 100, message = "취미는 100자 이하로 입력해주세요.")
        String hobby
) {}
