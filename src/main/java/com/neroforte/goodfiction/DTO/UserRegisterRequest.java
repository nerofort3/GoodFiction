package com.neroforte.goodfiction.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRegisterRequest(

        String username,

        @Email
        String email,

        @NotBlank(message = "password cannot be null")
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,32}$",
                message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
        String password
) {
}
