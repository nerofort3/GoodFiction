package com.neroforte.goodfiction.DTO;

import lombok.Data;

import java.util.List;

@Data
public class OpenLibrarySearchResponse {
    private int numFound;
    private List<OpenLibraryBookDoc> docs;
}