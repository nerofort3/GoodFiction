package com.neroforte.goodfiction.mapper;

import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {


    UserResponse userToUserResponse(UserEntity user);

    UserEntity userRegisterToUserEntity (UserRegisterRequest userRegisterRequest);
}
