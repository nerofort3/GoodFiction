package com.neroforte.goodfiction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

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

    @Column(unique = true)
    @NotNull(message = "name cannot be null")
    private String username;

    @Column(unique = true)
    @Email(message = "email cannot be null")
    private String email;

    @Column
    @NotBlank(message = "password cannot be blank")
    @Size(min = 8)
    private String password;

    @Column
    private LocalDateTime createdDate;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBookListItem> bookListItems = new ArrayList<>();


    @PrePersist
    private void autoSetCreatedDate(){
        this.createdDate = LocalDateTime.now();
    }

}
