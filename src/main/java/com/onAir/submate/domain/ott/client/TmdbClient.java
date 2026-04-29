package com.onAir.submate.domain.ott.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class TmdbClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public TmdbClient(
            @Value("${tmdb.base-url}") String baseUrl,
            @Value("${tmdb.api-key}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String searchMovies(String query, int page) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .queryParam("include_adult", false)
                .build().encode().toUri();

        return restClient.get().uri(uri).retrieve().body(String.class);
    }

    public String searchTv(String query, int page) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/search/tv")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .queryParam("include_adult", false)
                .build().encode().toUri();

        return restClient.get().uri(uri).retrieve().body(String.class);
    }

    public String getMovieWatchProviders(Long movieId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + movieId + "/watch/providers")
                .queryParam("api_key", apiKey)
                .build().toUri();

        return restClient.get().uri(uri).retrieve().body(String.class);
    }

    public String getTvWatchProviders(Long tvId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/tv/" + tvId + "/watch/providers")
                .queryParam("api_key", apiKey)
                .build().toUri();

        return restClient.get().uri(uri).retrieve().body(String.class);
    }
}
