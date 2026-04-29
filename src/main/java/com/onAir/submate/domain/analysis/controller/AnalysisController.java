package com.onAir.submate.domain.analysis.controller;

import com.onAir.submate.domain.analysis.dto.OptimizationResponse;
import com.onAir.submate.domain.analysis.service.SubscriptionAnalysisService;
import com.onAir.submate.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final SubscriptionAnalysisService subscriptionAnalysisService;

    /**
     * GET /api/analysis/optimize
     * 사용자의 구독 현황을 AI로 분석하여 중복 감지 + 비용 절감 추천 반환
     */
    @GetMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimize() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(subscriptionAnalysisService.analyze(userId));
    }
}
