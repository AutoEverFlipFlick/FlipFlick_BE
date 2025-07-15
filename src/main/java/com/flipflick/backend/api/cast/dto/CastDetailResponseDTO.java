package com.flipflick.backend.api.cast.dto;

import com.flipflick.backend.api.cast.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CastDetailResponseDTO {

    private Long tmdbId;
    private String name;
    private Gender gender;
    private String profileImage;
    private String placeOfBirth;
    private LocalDate birthday;
    private LocalDate deathday;
    private List<FilmographyDTO> filmographies;
}