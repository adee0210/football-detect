package com.loopy.footballvideoprocessor.user.service;

import java.util.UUID;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.user.dto.UserDTO;
import com.loopy.footballvideoprocessor.user.dto.UserUpdateDTO;
import com.loopy.footballvideoprocessor.user.model.User;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserById(UUID id);

    UserDTO getUserByUsername(String username);

    PagedResponse<UserDTO> getAllUsers(int page, int size);

    UserDTO updateUser(UUID id, User user);

    UserDTO updateUser(UUID id, UserUpdateDTO userUpdateDTO);

    void deleteUser(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User getCurrentUser();

    UserDTO mapToDTO(User user);
}
