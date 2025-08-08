package com.neroforte.goodfiction.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
public class UserResponse {
    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdDate;

}
