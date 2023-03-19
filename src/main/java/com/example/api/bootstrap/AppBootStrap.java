package com.example.api.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.context.SecurityContextHolder;
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
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
