package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenLibraryWork {

    private String title;

    private List<String> author_name;

    private Integer first_publish_year;

    private List<String> isbn;

    private List<Integer> covers;

    private String key;

    private double externalRating;

    private Description description;

    private List<String> subjects;

    //private List<String> excerpts;

    public String getSafeDescription() {
        return description != null && description.getValue() != null
                ? description.getValue()
                : "No description";
    }

}
