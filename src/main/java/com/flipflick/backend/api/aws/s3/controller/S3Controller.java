package com.flipflick.backend.api.aws.s3.controller;

import com.flipflick.backend.api.aws.s3.service.S3Service;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
@Tag(name = "Image - S3", description = "이미지 관련 API 입니다.")
public class S3Controller {
    private final S3Service s3Service;

    @Operation(summary = "이미지 업로드 API", description = "이미지를 받아서 저장 후 URL로 반환 합니다.")
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestPart MultipartFile file) throws IOException{

        String url = s3Service.upload(file, "images");
        return ApiResponse.success(SuccessStatus.IMAGE_UPLOAD_SUCCESS, url);

    }

}
