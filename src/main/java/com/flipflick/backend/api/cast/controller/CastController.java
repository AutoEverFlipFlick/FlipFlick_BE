package com.flipflick.backend.api.cast.controller;

import com.flipflick.backend.api.cast.dto.CastDetailResponseDTO;
import com.flipflick.backend.api.cast.dto.SearchRequestIdDTO;
import com.flipflick.backend.api.cast.service.CastService;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cast")
@Tag(name="Cast", description = "배우 관련 API 입니다.")
public class CastController {

    private final CastService castService;

    @Operation(summary = "배우 상세 조회 API", description = "배우 상세정보 및 필모그래피(영화만) 을 조회합니다.")
    @PostMapping("/view")
    public ResponseEntity<ApiResponse<CastDetailResponseDTO>> viewCastDetail(@RequestBody SearchRequestIdDTO searchRequestIdDTO) {

        CastDetailResponseDTO detail = castService.viewCastDetail(searchRequestIdDTO);
        return ApiResponse.success(SuccessStatus.SEND_CAST_DETAIL_SUCCESS, detail);
    }
}
