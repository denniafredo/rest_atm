package com.example.api.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.api.config.JwtTokenUtil;
import com.example.api.constant.Constant;
import com.example.api.dto.jwt.JwtErrorResponseDTO;
import com.example.api.dto.jwt.JwtLogoutResponseDTO;
import com.example.api.dto.jwt.JwtRequestDTO;
import com.example.api.dto.jwt.JwtResponseDTO;
import com.example.api.dto.user.CreateUserDTO;
import com.example.api.model.User;
import com.example.api.service.DebtService;
import com.example.api.service.JwtUserDetailsService;
import com.example.api.service.TransactionService;
import com.example.api.service.UserService;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class JwtAuthenticationController {

        @Autowired
        private JwtTokenUtil jwtTokenUtil;

        @Autowired
        private JwtUserDetailsService userDetailsService;

        @Autowired
        private final UserService userService;

        @Autowired
        private final TransactionService transactionService;

        @Autowired
        private final DebtService debtService;

        public JwtAuthenticationController(UserService userService, TransactionService transactionService,
                        DebtService debtService) {
                this.userService = userService;
                this.transactionService = transactionService;
                this.debtService = debtService;
        }

        @PostMapping(value = "/login")
        public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequestDTO authenticationRequest,
                        HttpSession session) {

                if (session.getAttribute("name") != null) {
                        if (!session.getAttribute("name").equals(authenticationRequest.getUsername())) {
                                return ResponseEntity.ok(
                                                new JwtErrorResponseDTO("Please wait, there is someone loggedin!"));
                        } else {
                                session.setMaxInactiveInterval(Constant.TIMEOUT);
                        }
                } else {
                        session.setMaxInactiveInterval(Constant.TIMEOUT);
                        session.setAttribute("name", authenticationRequest.getUsername());
                }

                List<String> messages = new ArrayList<String>();

                if (userService.isUsernameExist(authenticationRequest.getUsername())) {
                        CreateUserDTO newUser = new CreateUserDTO(authenticationRequest.getUsername(),
                                        authenticationRequest.getUsername(), authenticationRequest.getUsername());
                        userService.create(newUser);
                }
                User user = userService.findUserByUsername(authenticationRequest.getUsername());

                messages.add(String.format("Hello, %s!", user.getUsername()));
                messages.add(transactionService.balanceToString(user.getId()));
                messages.addAll(debtService.getAllDetailToString(user.getId()));
                return ResponseEntity.ok(
                                new JwtResponseDTO(
                                                jwtTokenUtil.generateToken(
                                                                userDetailsService.loadUserByUsername(
                                                                                authenticationRequest.getUsername())),
                                                messages));
        }

        @PostMapping(value = "/logout")
        public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails,
                        HttpSession session) {
                User user = userService.findUserByUsername(userDetails.getUsername());
                // must do something in here for 1 login only
                if (session.getAttribute("name") != null
                                && !session.getAttribute("name").equals(user.getUsername())) {
                        return ResponseEntity.ok(
                                        new JwtErrorResponseDTO("You are not logged in!"));
                }
                session.setAttribute("name", null);

                SecurityContextHolder.getContext().setAuthentication(null);
                return ResponseEntity.ok(
                                new JwtLogoutResponseDTO(String.format("Goodbye, %s", user.getUsername())));
        }

}
