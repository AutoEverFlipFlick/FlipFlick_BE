package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}
