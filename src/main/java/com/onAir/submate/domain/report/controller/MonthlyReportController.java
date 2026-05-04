package com.onAir.submate.domain.report.controller;

import com.onAir.submate.domain.report.dto.MonthlyReportResponse;
import com.onAir.submate.domain.report.service.MonthlyReportService;
import com.onAir.submate.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class MonthlyReportController {

    private final MonthlyReportService monthlyReportService;

    /**
     * POST /api/reports/generate
     * 이번 달 리포트 수동 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<MonthlyReportResponse> generateCurrentMonth() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(monthlyReportService.generateCurrentMonth(userId));
    }

    /**
     * POST /api/reports/{yearMonth}/regenerate
     * 특정 월 리포트 재생성
     */
    @PostMapping("/{yearMonth}/regenerate")
    public ResponseEntity<MonthlyReportResponse> regenerate(@PathVariable String yearMonth) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(monthlyReportService.regenerateReport(userId, yearMonth));
    }

    /**
     * GET /api/reports
     * 내 월간 리포트 목록 (최신순)
     */
    @GetMapping
    public ResponseEntity<List<MonthlyReportResponse>> getMyReports() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(monthlyReportService.getMyReports(userId));
    }

    /**
     * GET /api/reports/2026-04
     * 특정 월 리포트 조회
     */
    @GetMapping("/{yearMonth}")
    public ResponseEntity<MonthlyReportResponse> getReport(
            @PathVariable String yearMonth) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(monthlyReportService.getReport(userId, yearMonth));
    }
}
