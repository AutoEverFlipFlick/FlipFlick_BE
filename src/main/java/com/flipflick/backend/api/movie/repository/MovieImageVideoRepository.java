package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.MovieImageVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieImageVideoRepository extends JpaRepository<MovieImageVideo, Long> {
}
