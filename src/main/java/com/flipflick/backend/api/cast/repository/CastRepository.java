package com.flipflick.backend.api.cast.repository;

import com.flipflick.backend.api.cast.entity.Cast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CastRepository extends JpaRepository<Cast, Long> {

    // 배우 + 필모그래피를 한 번에 조회
    @Query("select c from Cast c left join fetch c.filmographies f where c.tmdbId = :tmdbId")
    Optional<Cast> findWithFilmographiesByTmdbId(@Param("tmdbId") Long tmdbId);
}
