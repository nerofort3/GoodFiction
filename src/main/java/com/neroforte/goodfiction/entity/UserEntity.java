package com.neroforte.goodfiction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotNull(message = "name cannot be null")
    private String username;

    @Column(unique = true, nullable = false)
    @NotNull(message = "email cannot be null")
    @Email(message = "email must be a valid email address")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "password cannot be blank")
    @Size(min = 8)
    private String password;

    @Column
    private String roles;

    @Column(nullable = false)
    @Builder.Default
    private boolean isProfilePublic = true;

    @Column
    private Instant createdDate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserBookListItem> bookListItems = new ArrayList<>();


    @PrePersist
    private void autoSetCreatedDate(){
        this.createdDate = Instant.now();
    }

}
