package com.flipflick.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "movie_image_video")
@AllArgsConstructor
public class MovieImageVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Enumerated(EnumType.STRING)
    private MovieMediaType movieMediaType;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movie_id")
    private Movie movie;
}
