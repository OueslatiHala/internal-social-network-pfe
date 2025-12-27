package com.hala.authentification.mappers;

import com.hala.authentification.dto.UserDTO;
import com.hala.authentification.entities.User;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User userDtoToUser(UserDTO userDTO);

    List<UserDTO> userToDto(List<User> users);
    default UserDTO userToUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .phoneNumber(user.getPhoneNumber())
                .companyName(user.getCompanyName())
                .archived(user.getArchived())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .logo(
                        user.getLogo() != null && !user.getLogo().startsWith("http")
                                ? "http://localhost:8070/api/v1/users/downloadFile/" + user.getLogo().replace("/profile-photos/", "")
                                : user.getLogo()
                )
                .build();
    }


}
