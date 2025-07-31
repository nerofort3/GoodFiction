package com.neroforte.goodfiction.entity;

import com.neroforte.goodfiction.BookStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
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


    @Column(unique = true,nullable = false)
    @NotBlank
    private String openLibraryKey;


    @Column(length = 100)
    @NotBlank
    private int coverID;

    @Column
    @NotNull
    private int firstPublishYear;

    @Column(unique = true,nullable = false)
    @NotBlank
    private String isbn;


    @Column
    @NotNull
    private double externalRating;

    @Column(columnDefinition = "TEXT")
    @NotBlank
    private String description;


    /*private Rating rating;
        TODO add average rating based on the this website`s users score
    */
}
