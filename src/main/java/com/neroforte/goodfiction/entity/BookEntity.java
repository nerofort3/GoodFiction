package com.neroforte.goodfiction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "books")
@NoArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 100)
    @NotBlank
    private String title;

    @Column(length = 100)
    @NotBlank
    private String author;

    @Column
    @NotNull
    private double externalRating;

    @Column
    @NotBlank
    @Size(max = 200)
    private String description;


    /*private Rating rating;
        TODO add average rating based on the this website`s users score
    */

    public BookEntity(String title, String author, double externalRating) {
        this.title = title;
        this.author = author;
        this.externalRating = externalRating;
    }

}
