package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.BookStatus;
import com.neroforte.goodfiction.entity.UserBookListItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBookListItemResponse {

    private Long id;

    private Long userId;

    private Long bookId;

    private String bookTitle;

    private String username;

    private BookStatus bookStatus;

    private Integer userRating;

    private Double finishedPercentage;

    private String review;

}
