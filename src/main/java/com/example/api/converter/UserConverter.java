package com.example.api.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.example.api.dto.user.UserDTO;
import com.example.api.model.User;

@Component
public class UserConverter extends BaseConverter<User, UserDTO>{
    @Override
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) return null;
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        return user;
    }

    @Override
    public UserDTO toDto(User e) {
        if (e == null) return null;
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(e,userDTO);
        return userDTO;
    }
}

