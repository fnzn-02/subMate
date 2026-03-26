package com.onAir.submate.domain.dashboard.controller;

import com.onAir.submate.domain.dashboard.dto.DashboardResponse;
import com.onAir.submate.domain.dashboard.service.DashboardService;
import com.onAir.submate.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
