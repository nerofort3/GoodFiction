package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookResponse(
        String googleId,

        String title,

        String author,

        String isbn,

        String thumbnailUrl,

        String publishedDate,

        String description,

        Integer pageCount,

        double externalRating,

        List<String> categories
) {
}