package com.onAir.submate.domain.report.dto;

import com.onAir.submate.domain.report.entity.MonthlyReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public record MonthlyReportResponse(
        Long id,
        String yearMonth,
        List<StatCard> cards,
        String summary,
        String detail,
        BigDecimal totalAmount,
        BigDecimal savedAmount,
        LocalDateTime createdAt
) {
    public record StatCard(String label, String value, String icon) {}

    public static MonthlyReportResponse from(MonthlyReport report) {
        String content = report.getContent();

        // [CARDS] 파싱
        List<StatCard> cards = new java.util.ArrayList<>();
        int start = content.indexOf("[CARDS]");
        int end = content.indexOf("[/CARDS]");
        if (start != -1 && end != -1) {
            String block = content.substring(start + 7, end).trim();
            for (String line : block.split("\n")) {
                String[] p = line.trim().split("\\|");
                if (p.length == 3) cards.add(new StatCard(p[0].trim(), p[1].trim(), p[2].trim()));
            }
        }

        // totalAmount 카드가 없으면 기본 카드 추가
        if (cards.isEmpty()) {
            cards.add(new StatCard("총 월 지출",
                    "₩" + report.getTotalAmount().setScale(0, RoundingMode.HALF_UP).toPlainString(),
                    "wallet"));
        }

        String withoutCards = content.replaceAll("(?s)\\[CARDS\\].*?\\[/CARDS\\]", "").trim();
        String[] parts = withoutCards.split("===DETAIL===", 2);
        String summary = parts[0].trim();
        String detail = parts.length > 1 ? parts[1].trim() : withoutCards;

        return new MonthlyReportResponse(
                report.getId(),
                report.getYearMonth(),
                cards,
                summary,
                detail,
                report.getTotalAmount(),
                report.getSavedAmount(),
                report.getCreatedAt()
        );
    }
}
