package com.flipflick.backend.api.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchRequestDTO {

    private String query;
    private int page;
}
