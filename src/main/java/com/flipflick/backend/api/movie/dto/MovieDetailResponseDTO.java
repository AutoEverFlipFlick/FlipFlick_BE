package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MovieDetailResponseDTO {

    private Long movieId;
    private Long tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterImg;
    private String backgroundImg;
    private double popcorn;
    private double voteAverage;
    private LocalDate releaseDate;
    private int runtime;
    private int productionYear;
    private String productionCountry;
    private String ageRating;
    private boolean myLike;
    private boolean myHate;
    private boolean myWatched;
    private boolean myBookmark;

    private List<GenreDTO> genres;
    private List<String> images;
    private List<String> videos;
    private List<ProviderDTO> providers;
    private List<CastResponseDTO> casts;
}
