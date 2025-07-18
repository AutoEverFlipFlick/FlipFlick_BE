package com.flipflick.backend.api.movie.dto;

import com.flipflick.backend.api.review.entity.LikeHateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieLikeHateRequestDTO {

    private Long movieId;
    private LikeHateType likeHateType;
}
