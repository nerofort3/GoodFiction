package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.entity.BookEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
public record OpenLibraryBookDoc(
        String title,

        List<String> authorName,

        Integer firstPublishYear,

        List<String> isbn,

        Integer coverI,

        String key,

        Integer ratingsCount,

        String description
) {
}
