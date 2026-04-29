package com.onAir.submate.domain.ott.dto;

import java.util.List;

public record ContentWithProvidersDto(
        OttContentDto content,
        List<WatchProviderDto> providers,
        String watchLink       // TMDB Just Watch 링크
) {}
