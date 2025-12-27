package com.hala.authentification.Iservice;
import com.hala.authentification.dto.UserDTO;
import com.hala.authentification.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface IServiceUser {
    UserDTO getUserById(Integer userId);

    List<UserDTO> getAllUsers();

    User updateProfilePhoto(Integer userId, MultipartFile profilePhoto);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Integer userId, UserDTO userDTO);

    void deleteUser(Integer userId);
    UserDTO consulterProfile(Integer userId);

}
