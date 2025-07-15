package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByTmdbId(Long tmdbId);
    List<Genre> findByTmdbIdIn(List<Long> tmdbIds);
}
