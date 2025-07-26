package com.neroforte.goodfiction.entity;

import com.neroforte.goodfiction.DTO.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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


    @Column
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Book> books = new HashSet<>();


    @PrePersist
    private void autoSetCreatedDate(){
        this.createdDate = LocalDateTime.now();
    }


    public UserEntity(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }


}
