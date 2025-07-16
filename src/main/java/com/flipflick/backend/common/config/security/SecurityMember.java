package com.flipflick.backend.common.config.security;

import com.flipflick.backend.api.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityMember{
    private Long id;
    private String email;
    private String password;
    private Role role;


}