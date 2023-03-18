package com.example.api.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.api.dto.user.CreateUserDTO;
import com.example.api.service.UserService;

@Component
public class AppBootStrap implements CommandLineRunner {
    private UserService userService;

    @Autowired
    public AppBootStrap(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        CreateUserDTO user1 = CreateUserDTO.builder()
                .username("Alice")
                .password("Alice")
                .accountNumber("1")
                .build();

        CreateUserDTO user2 = CreateUserDTO.builder()
                .username("Bob")
                .password("Bob")
                .accountNumber("2")
                .build();
        userService.create(user1);
        userService.create(user2);
    }
}
