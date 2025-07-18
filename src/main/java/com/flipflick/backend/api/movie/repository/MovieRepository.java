package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = {
            "movieGenres.genre",
            "media",
            "providers.provider"
    })
    Optional<Movie> findWithAllByTmdbId(Long tmdbId);
    Optional<Movie> findByTmdbId(Long tmdbId);
}
