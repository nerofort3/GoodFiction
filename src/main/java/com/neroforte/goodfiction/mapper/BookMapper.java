package com.neroforte.goodfiction.mapper;


import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.GoogleBooksResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.awt.print.Book;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    BookResponse bookToBookResponse(BookEntity book, List<String> subjects);

    BookResponse bookToBookResponse(BookEntity book);

    BookResponse toResponse(BookEntity entity);

    @Mapping(target = "googleId", source = "id")
    @Mapping(target = "title", source = "volumeInfo.title")
    @Mapping(target = "publishedDate", source = "volumeInfo.publishedDate")
    @Mapping(target = "description", source = "volumeInfo.description")
    @Mapping(target = "pageCount", source = "volumeInfo.pageCount")
    @Mapping(target = "categories", source = "volumeInfo.categories")
    @Mapping(target = "author", source = "volumeInfo.authors", qualifiedByName = "firstAuthor")
    @Mapping(target = "isbn", source = "volumeInfo.industryIdentifiers", qualifiedByName = "findIsbn13")
    @Mapping(target = "thumbnailUrl", source = "volumeInfo.imageLinks", qualifiedByName = "secureThumbnail")
    @Mapping(target = "id", ignore = true) // Ignore DB ID, it's auto-generated
    BookEntity toEntity(GoogleBooksResponse.Item item);

    @Named("firstAuthor")
    default String mapFirstAuthor(List<String> authors) {
        return (authors != null && !authors.isEmpty()) ? authors.get(0) : "Unknown Author";
    }

    @Named("findIsbn13")
    default String mapIsbn(List<GoogleBooksResponse.IndustryIdentifier> identifiers) {
        if (identifiers == null) return null;
        return identifiers.stream()
                .filter(id -> "ISBN_13".equals(id.type()))
                .map(GoogleBooksResponse.IndustryIdentifier::identifier)
                .findFirst()
                .orElse(null);
    }

    @Named("secureThumbnail")
    default String mapThumbnail(GoogleBooksResponse.ImageLinks links) {
        if (links == null) return null;
        String url = links.thumbnail();
        return (url != null && url.startsWith("http:")) ? url.replace("http:", "https:") : url;
    }

}
