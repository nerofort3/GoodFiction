package com.neroforte.goodfiction.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserUpdateRequest(

        @NotBlank
        String username,

        @NotBlank
        String email
) {
}
