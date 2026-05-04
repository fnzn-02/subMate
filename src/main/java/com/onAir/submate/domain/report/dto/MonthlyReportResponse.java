package com.onAir.submate.domain.report.dto;

import com.onAir.submate.domain.report.entity.MonthlyReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonthlyReportResponse(
        Long id,
        String yearMonth,
        String summary,
        String detail,
        BigDecimal totalAmount,
        BigDecimal savedAmount,
        LocalDateTime createdAt
) {
    public static MonthlyReportResponse from(MonthlyReport report) {
        String[] parts = report.getContent().split("===DETAIL===", 2);
        String summary = parts[0].trim();
        String detail = parts.length > 1 ? parts[1].trim() : report.getContent();
        return new MonthlyReportResponse(
                report.getId(),
                report.getYearMonth(),
                summary,
                detail,
                report.getTotalAmount(),
                report.getSavedAmount(),
                report.getCreatedAt()
        );
    }
}
