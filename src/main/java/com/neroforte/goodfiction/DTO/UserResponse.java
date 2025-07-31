package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdDate;

    private Set<OpenLibraryBookDoc> openLibraryBookDocs = new HashSet<>();


    public static UserResponse entityToUserResponse(UserEntity userEntity) {

        return UserResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .createdDate(userEntity.getCreatedDate())
                .build();
    }

}
