package com.neroforte.goodfiction.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Password {

    @NotBlank
    @Size(min = 8, max = 32)
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 32)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,32}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
    private String newPassword;
}
