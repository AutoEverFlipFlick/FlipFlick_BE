package com.flipflick.backend.api.cast.repository;

import com.flipflick.backend.api.cast.entity.Filmography;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmographyRepository extends JpaRepository<Filmography, Long> {
}
