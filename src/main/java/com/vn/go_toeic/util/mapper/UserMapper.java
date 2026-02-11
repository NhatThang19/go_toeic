package com.vn.go_toeic.util.mapper;

import com.vn.go_toeic.dto.UserRegisterReq;
import com.vn.go_toeic.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "socialAccounts", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    User toEntity(UserRegisterReq req);
}