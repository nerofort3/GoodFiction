package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.neroforte.goodfiction.entity.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenLibraryWork(

        String title,

        List<String> author_name,

        Integer first_publish_year,

        List<String> isbn,

        List<Integer> covers,

        String key,

        Double externalRating,

        Description description,

        List<String> subjects

) {
    public String getSafeDescription() {
        return description != null && description.getValue() != null
                ? description.getValue()
                : "No description";
    }
}
