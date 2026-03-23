package com.neroforte.goodfiction.DTO;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserResponse(
        Long id,

        String username,

        String email,

        Instant createdDate,

        boolean isAdmin

) {}
