package com.neroforte.goodfiction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    @NotNull(message = "name cannot be null")
    private String username;

    @Column(unique = true)
    @Email(message = "email cannot be null")
    private String email;

    @Column
    @NotBlank(message = "password cannot be null")
    @Size(min = 8)
    private String password;

    @Column
    private LocalDateTime createdDate;

    @ManyToMany
    private Set<BookEntity> wantToRead = new HashSet<>();

    @ManyToMany
    private Set<BookEntity> currentlyReading= new HashSet<>();

    @ManyToMany
    private Set<BookEntity> read= new HashSet<>();


    public void addBookToRead(BookEntity bookEntity) {
       this.read.add(bookEntity);
    }

    public void addBookToWantToRead(BookEntity bookEntity) {
        this.wantToRead.add(bookEntity);
    }

    public void addBookToCurrentlyReading(BookEntity bookEntity) {
        this.currentlyReading.add(bookEntity);
    }



    public UserEntity(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdDate = LocalDateTime.now();
    }


}
