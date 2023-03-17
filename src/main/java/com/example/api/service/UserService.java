package com.example.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.config.JwtTokenUtil;
import com.example.api.converter.UserConverter;
import com.example.api.dao.UserRepository;
import com.example.api.dto.user.CreateUserDTO;
import com.example.api.dto.user.UserDTO;
import com.example.api.model.User;

import java.util.List;
import java.util.Optional;

import static org.hibernate.internal.CoreLogging.logger;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordHashService passwordHashService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            UserConverter userConverter,
            PasswordHashService passwordHashService,
            JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.passwordHashService = passwordHashService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<UserDTO> getAll() {
        return userConverter.toDto(userRepository.findAll());
    }

    public UserDTO get(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(userConverter::toDto).orElse(null);
    }

    public UserDTO getByName(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.map(userConverter::toDto).orElse(null);
    }

    public String create(CreateUserDTO createUserDTO) {
        try {
            if (isUsernameExist(createUserDTO.getUsername()))
                return String.format("Username : %s already exists", createUserDTO.getUsername());

            User user = User.builder()
                    .id(findNextId())
                    .username(createUserDTO.getUsername())
                    .passwordHash(passwordHashService.hashPassword(createUserDTO.getPassword()))
                    .accountNumber(createUserDTO.getAccountNumber())
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            logger(e.getMessage());
            return String.format("Failed create user with username : %s", createUserDTO.getUsername());
        }

        return String.format("Success create user with username : %s", createUserDTO.getUsername());
    }

    public boolean isUsernameExist(String username) {
        return userRepository.findByUsernameAndIsActiveIsTrue(username).isPresent();
    }

    public boolean isAccountNUmberExist(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber).isPresent();
    }

    public UserDTO findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsernameAndIsActiveIsTrue(username);

        return userOptional.map(userConverter::toDto).orElse(null);
    }

    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public long findNextId() {
        return userRepository.findNextId();
    }

    public UserDTO me(String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        return findByUsername(username);
    }
}
