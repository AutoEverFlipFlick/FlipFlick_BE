package com.flipflick.backend.api.passwordReset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendResetLinkRequestDto {
    private String email;
}