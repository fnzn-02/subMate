package com.onAir.submate.domain.report.dto;

import com.onAir.submate.domain.report.entity.MonthlyReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonthlyReportResponse(
        Long id,
        String yearMonth,
        String content,
        BigDecimal totalAmount,
        BigDecimal savedAmount,
        LocalDateTime createdAt
) {
    public static MonthlyReportResponse from(MonthlyReport report) {
        return new MonthlyReportResponse(
                report.getId(),
                report.getYearMonth(),
                report.getContent(),
                report.getTotalAmount(),
                report.getSavedAmount(),
                report.getCreatedAt()
        );
    }
}
