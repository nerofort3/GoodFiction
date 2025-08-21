package com.neroforte.goodfiction.mapper;


import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.entity.UserBookListItem;
import com.neroforte.goodfiction.service.BookService;
import com.neroforte.goodfiction.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",uses = {UserService.class , BookService.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserBookListMapper {


    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    UserBookListItemResponse userBookListToUserBookListItemResponse(UserBookListItem userBookListItem);

}
