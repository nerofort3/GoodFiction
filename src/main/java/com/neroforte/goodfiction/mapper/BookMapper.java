package com.neroforte.goodfiction.mapper;


import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    @Mapping(target = "subjects", ignore = true)
    BookResponse bookToBookResponse(BookEntity book);

}
