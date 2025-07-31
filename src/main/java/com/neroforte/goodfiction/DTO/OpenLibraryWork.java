package com.neroforte.goodfiction.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenLibraryWork {

    private String title;

    private List<String> author_name;

    private Integer first_publish_year;

    private List<String> isbn;

    private Integer cover_i;

    private String key;

    private double externalRating;

    private String description;

    private List<String> subjects;

    private List<String> excerpts;

}
