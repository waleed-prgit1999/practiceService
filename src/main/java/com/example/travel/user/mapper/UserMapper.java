package com.example.travel.user.mapper;

import com.example.travel.user.dto.UserResponse;
import com.example.travel.user.entity.Role;
import com.example.travel.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        Set<Role> roles = user.getRoles();
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                roles.stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
