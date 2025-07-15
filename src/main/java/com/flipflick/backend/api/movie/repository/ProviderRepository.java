package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByTmdbId(Long tmdbId);
    List<Provider> findByTmdbIdIn(List<Long> tmdbIds);

}
