package com.neroforte.goodfiction.entity;

import com.neroforte.goodfiction.BookStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;
@Entity
@Table(name = "user_books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookListItem {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;


    @ManyToOne
    @JoinColumn(name="book_id", nullable = false)
    private BookEntity book;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

//    @Column
//    private Integer userRating;




}
