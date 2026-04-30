package com.onAir.submate.domain.ott.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onAir.submate.domain.ott.client.TmdbClient;
import com.onAir.submate.domain.ott.dto.ContentWithProvidersDto;
import com.onAir.submate.domain.ott.dto.OttContentDto;
import com.onAir.submate.domain.ott.dto.WatchProviderDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OttService {

    private final TmdbClient tmdbClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    private static final String COUNTRY_CODE = "KR";

    public List<ContentWithProvidersDto> search(String query) {
        List<CompletableFuture<ContentWithProvidersDto>> futures = new ArrayList<>();

        // 영화 검색
        try {
            JsonNode movies = parse(tmdbClient.searchMovies(query, 1)).path("results");
            for (int i = 0; i < Math.min(movies.size(), 5); i++) {
                final JsonNode movie = movies.get(i);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    OttContentDto content = toMovieDto(movie);
                    List<WatchProviderDto> providers = getMovieProviders(content.id());
                    if (providers.isEmpty()) return null;
                    return new ContentWithProvidersDto(content, providers, getMovieWatchLink(content.id()));
                }, executor));
            }
        } catch (Exception e) {
            log.warn("영화 검색 실패: query={}", query, e);
        }

        // TV 검색
        try {
            JsonNode tvShows = parse(tmdbClient.searchTv(query, 1)).path("results");
            for (int i = 0; i < Math.min(tvShows.size(), 5); i++) {
                final JsonNode tv = tvShows.get(i);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    OttContentDto content = toTvDto(tv);
                    List<WatchProviderDto> providers = getTvProviders(content.id());
                    if (providers.isEmpty()) return null;
                    return new ContentWithProvidersDto(content, providers, getTvWatchLink(content.id()));
                }, executor));
            }
        } catch (Exception e) {
            log.warn("TV 검색 실패: query={}", query, e);
        }

        // 모든 병렬 요청 완료 대기 후 null 제거
        return futures.stream()
                .map(f -> {
                    try { return f.get(); } catch (Exception e) { return null; }
                })
                .filter(r -> r != null)
                .toList();
    }

    private List<WatchProviderDto> getMovieProviders(Long movieId) {
        try {
            return extractProviders(parse(tmdbClient.getMovieWatchProviders(movieId)));
        } catch (Exception e) {
            log.warn("영화 watch provider 조회 실패: movieId={}", movieId, e);
            return List.of();
        }
    }

    private List<WatchProviderDto> getTvProviders(Long tvId) {
        try {
            return extractProviders(parse(tmdbClient.getTvWatchProviders(tvId)));
        } catch (Exception e) {
            log.warn("TV watch provider 조회 실패: tvId={}", tvId, e);
            return List.of();
        }
    }

    private List<WatchProviderDto> extractProviders(JsonNode response) {
        List<WatchProviderDto> providers = new ArrayList<>();
        JsonNode krNode = response.path("results").path(COUNTRY_CODE);
        if (krNode.isMissingNode()) return providers;

        JsonNode flatrate = krNode.path("flatrate");
        if (!flatrate.isMissingNode() && flatrate.isArray()) {
            for (JsonNode p : flatrate) {
                providers.add(new WatchProviderDto(
                        p.path("provider_id").asInt(),
                        p.path("provider_name").asText(),
                        p.path("logo_path").isNull() ? null
                                : imageBaseUrl + p.path("logo_path").asText()
                ));
            }
        }
        return providers;
    }

    private JsonNode parse(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    private String getMovieWatchLink(Long movieId) {
        return "https://www.themoviedb.org/movie/" + movieId + "/watch?locale=" + COUNTRY_CODE;
    }

    private String getTvWatchLink(Long tvId) {
        return "https://www.themoviedb.org/tv/" + tvId + "/watch?locale=" + COUNTRY_CODE;
    }

    private OttContentDto toMovieDto(JsonNode node) {
        return new OttContentDto(
                node.path("id").asLong(),
                node.path("title").asText(),
                "movie",
                node.path("poster_path").isNull() ? null : imageBaseUrl + node.path("poster_path").asText(),
                node.path("vote_average").asDouble(),
                node.path("overview").asText()
        );
    }

    private OttContentDto toTvDto(JsonNode node) {
        return new OttContentDto(
                node.path("id").asLong(),
                node.path("name").asText(),
                "tv",
                node.path("poster_path").isNull() ? null : imageBaseUrl + node.path("poster_path").asText(),
                node.path("vote_average").asDouble(),
                node.path("overview").asText()
        );
    }
}
