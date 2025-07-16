package com.flipflick.backend.api.playlist.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Entity
@Table(name = "movie_playlist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoviePlaylist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_list_id", nullable = false)
    private PlayList playList;

    @Column(name = "tmdb_id", nullable = false)
    private Integer tmdbId;

    @Column(name = "title", length = 200)  // title 필드 추가
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "poster_url")
    private String posterUrl;

    @Builder
    public MoviePlaylist(PlayList playList, Integer tmdbId, String title, LocalDate releaseDate, String posterUrl) {
        this.playList = playList;
        this.tmdbId = tmdbId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
    }
}