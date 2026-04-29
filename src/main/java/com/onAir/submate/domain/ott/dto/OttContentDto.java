package com.onAir.submate.domain.ott.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OttContentDto(
        Long id,
        String title,
        String type,          // "movie" or "tv"
        String posterUrl,
        Double voteAverage,
        String overview
) {
    // TMDB 영화 응답 → OttContentDto
    public static OttContentDto fromMovie(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("overview") String overview,
            String imageBaseUrl) {
        return new OttContentDto(id, title, "movie",
                posterPath != null ? imageBaseUrl + posterPath : null,
                voteAverage, overview);
    }

    // TMDB TV 응답 → OttContentDto
    public static OttContentDto fromTv(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("overview") String overview,
            String imageBaseUrl) {
        return new OttContentDto(id, name, "tv",
                posterPath != null ? imageBaseUrl + posterPath : null,
                voteAverage, overview);
    }
}
