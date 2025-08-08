package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
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



    private double externalRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> subjects;

}
