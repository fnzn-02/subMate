package com.onAir.submate.domain.analysis.dto;

import java.util.List;

public record OptimizationResponse(
        List<StatCard> cards,
        String summary,
        String detail
) {
    public record StatCard(String label, String value, String icon) {}
}
