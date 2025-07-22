package com.neroforte.goodfiction.model;

import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.entity.UserEntity;
import jakarta.persistence.ManyToMany;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdDate;

    private Set<BookEntity> wantToRead = new HashSet<>();

    private Set<BookEntity> currentlyReading= new HashSet<>();

    private Set<BookEntity> read= new HashSet<>();


    public User(Long id, String username, String email ){
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdDate = LocalDateTime.now();
    }
    public User(Long id, String username, String email, LocalDateTime createdDate ){
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdDate = createdDate;
    }



    public static User entityToUser(UserEntity userEntity) {

        User newUser = new User(userEntity.getId(),userEntity.getUsername(),userEntity.getEmail(),userEntity.getCreatedDate());
        newUser.setRead(new HashSet<BookEntity>());
        newUser.setWantToRead(new HashSet<BookEntity>());
        newUser.setCurrentlyReading(new HashSet<BookEntity>());
        return newUser;
    }

}
