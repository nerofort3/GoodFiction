package com.neroforte.goodfiction.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record OpenLibrarySearchResponse(

        int numFound,
        List<OpenLibraryBookDoc> docs
) {
}