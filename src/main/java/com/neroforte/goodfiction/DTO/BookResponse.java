package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.entity.BookEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {

    private String title;

    private String author;

    private String openLibraryKey;

    private int cover_i;

    private Integer firstPublishYear;

    private String isbn;

    private String description;

    private double rating;

    private List<String> subjects;



//    private List<String> excerpts;

    public static BookResponse bookEntityToBookResponse(BookEntity entity) {
        return BookResponse.builder()
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .openLibraryKey(entity.getOpenLibraryKey())
                .cover_i(entity.getCover_i())
                .firstPublishYear(entity.getFirstPublishYear())
                .isbn(entity.getIsbn())
                .description(entity.getDescription())
                .rating(entity.getExternalRating())
                .build();
    }
}
