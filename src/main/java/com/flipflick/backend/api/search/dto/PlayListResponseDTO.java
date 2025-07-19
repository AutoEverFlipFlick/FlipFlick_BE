package com.flipflick.backend.api.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayListResponseDTO {

    private Long playListId;
    private String title;
    private String nickname;
    private String thumbnailUrl;
    private Integer movieCount;
    private Integer bookmarkCount;
}
