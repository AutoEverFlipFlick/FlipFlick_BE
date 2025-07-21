package com.flipflick.backend.api.movie.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "movie")
public class Movie extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long tmdbId;            // TMDB 영화 ID

    private String title;           // 제목
    private String originalTitle;   // 원제목

    @Lob
    @Column(columnDefinition = "CLOB")
    private String overview;        // 줄거리

    private String posterImg;       // 포스터
    private String backgroundImg;   // 배경 이미지 (backdrop)

    private double popcorn;         // 팝콘지수
    private double voteAverage;     // 우리 자체 평점 (초기 0)

    private long likeCnt;           // 좋아요
    private long hateCnt;           // 싫어요

    private LocalDate releaseDate;  // 개봉일
    private int runtime;            // 상영 시간

    private int productionYear;     // 제작연도
    private String productionCountry; // 제작국가
    private String ageRating;      // 연령등급

    @BatchSize(size = 50)
    @OrderColumn(name = "genre_order")
    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenre> movieGenres = new ArrayList<>();

    @BatchSize(size = 50)
    @OrderColumn(name = "media_order")
    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieImageVideo> media = new ArrayList<>();

    @BatchSize(size = 50)
    @OrderColumn(name = "provider_order")
    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieProvider> providers = new ArrayList<>();

    public void updateVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void incrementLike() { this.likeCnt++; }
    public void decrementLike() { if(this.likeCnt>0) this.likeCnt--; }
    public void incrementHate() { this.hateCnt++; }
    public void decrementHate() { if(this.hateCnt>0) this.hateCnt--; }
    public void updatePopcornScore(double newScore) { this.popcorn = newScore; }
}
