package com.onAir.submate.domain.analysis.dto;

public record OptimizationResponse(
        String analysis   // Gemini가 반환한 마크다운 형식의 분석 결과
) {}
