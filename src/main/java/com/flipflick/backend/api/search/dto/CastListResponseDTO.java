package com.flipflick.backend.api.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CastListResponseDTO {

    private Long tmdbId;
    private String name;
    private String profileImage;
    private List<String> knownFor;
}
