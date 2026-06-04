package com.neroforte.goodfiction.mapper;

import com.neroforte.goodfiction.DTO.ActivityFeedResponse;
import com.neroforte.goodfiction.entity.ActivityFeedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivityFeedMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "googleId", source = "book.googleId")
    ActivityFeedResponse toDto(ActivityFeedEntity entity);
}
