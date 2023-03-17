package com.example.api.service;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.api.dto.user.UserDTO;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDTO userDTO = userService.findByUsername(username);
        if (Objects.nonNull(userDTO)) {
            return new User(
                    userDTO.getUsername(),
                    userDTO.getPasswordHash(),
                    new ArrayList<>());
        } else
            throw new UsernameNotFoundException(String.format("User with username: %s not found", username));
    }
}
