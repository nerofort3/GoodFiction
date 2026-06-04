package com.neroforte.goodfiction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.userdetails.User;
@Entity
@Table(name = "user_books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookListItem {

    @Id
    @GeneratedValue
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name="book_id", nullable = false)
    private BookEntity book;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

    @Column
    private Integer userRating;

    @Column
    private Double finishedPercentage;

    @Column(length = 500)
    private String review;


}
