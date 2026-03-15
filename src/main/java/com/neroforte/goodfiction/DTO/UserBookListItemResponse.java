package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.entity.BookStatus;
import lombok.Builder;

@Builder
public record UserBookListItemResponse(
        Long id,
        Long userId,
        String googleId,
        String bookTitle,
        String username,
        BookStatus bookStatus,
        Integer userRating,
        double finishedPercentage,
        String review
) {
}