package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// 1. The Root Container
@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBooksResponse(
        List<Item> items,
        int totalItems
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String id,
            VolumeInfo volumeInfo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VolumeInfo(
            String title,
            List<String> authors,
            String publisher,
            String publishedDate,
            String description,
            List<IndustryIdentifier> industryIdentifiers,
            Integer pageCount,
            List<String> categories,
            Double averageRating,
            ImageLinks imageLinks,
            String language
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IndustryIdentifier(
            String type,
            String identifier
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageLinks(
            String smallThumbnail,
            String thumbnail
    ) {}
}