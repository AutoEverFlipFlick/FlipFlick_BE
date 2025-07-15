package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProviderDTO {
    private String providerName;
    private String providerType;
}
