package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.MovieProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieProviderRepository extends JpaRepository<MovieProvider, Long> {
}
