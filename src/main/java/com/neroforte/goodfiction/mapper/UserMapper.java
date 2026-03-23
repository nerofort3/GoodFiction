package com.neroforte.goodfiction.mapper;

import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {


    @Mapping(target = "isAdmin", source = "roles", qualifiedByName = "checkIfAdmin")
    UserResponse userToUserResponse(UserEntity user);

    UserEntity userRegisterToUserEntity (UserRegisterRequest userRegisterRequest);

    @Named("checkIfAdmin")
    default boolean checkIfAdmin(String role) {
        return role.contains("ROLE_ADMIN");
    }
}
