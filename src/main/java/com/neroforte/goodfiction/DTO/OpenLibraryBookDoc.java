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
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenLibraryBookDoc {
    private String title;

    private List<String> author_name;

    private Integer first_publish_year;

    private List<String> isbn;

    private Integer cover_i;

    private String key;

    private Integer ratings_count;

//    private String description;

}
