package com.example.api.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.example.api.config.JwtTokenUtil;
import com.example.api.constant.Constant;
import com.example.api.dto.jwt.JwtLogoutResponseDTO;
import com.example.api.dto.jwt.JwtRequestDTO;
import com.example.api.dto.jwt.JwtResponseDTO;
import com.example.api.dto.transaction.ResponseDTO;
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
        private JwtUserDetailsService jwtUserDetailsService;

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
        public ResponseEntity<?> createAuthenticationToken(HttpServletRequest request,
                        @RequestBody JwtRequestDTO authenticationRequest) {
                List<String> messages = new ArrayList<String>();
                String username = authenticationRequest.getUsername();
                if (!userService.isUsernameExist(username)) {
                        CreateUserDTO newUser = new CreateUserDTO(username, username, username);
                        userService.create(newUser);
                }
                User user = userService.findUserByUsername(username);

                String token = jwtTokenUtil.generateToken(
                                jwtUserDetailsService.loadUserByUsername(user.getUsername()));

                if (SecurityContextHolder.getContext().getAuthentication() == null
                                || SecurityContextHolder.getContext().getAuthentication().getName()
                                                .equals("anonymousUser")) {
                        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.getUsername());
                        if (Boolean.TRUE.equals(jwtTokenUtil.validateToken(token, userDetails))) {
                                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                usernamePasswordAuthenticationToken
                                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext()
                                                .setAuthentication(usernamePasswordAuthenticationToken);
                        }
                }
                if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(user.getUsername())
                                && !SecurityContextHolder.getContext().getAuthentication().getName()
                                                .equals("anonymousUser")) {
                        messages.add("Please wait, there is someone loggedin!");
                        System.out.println(SecurityContextHolder.getContext().getAuthentication());
                        return ResponseEntity.ok(new ResponseDTO(messages));
                } else {
                        messages.add(String.format("Hello, %s!", user.getUsername()));
                        messages.add(transactionService.balanceToString(user.getId()));
                        messages.addAll(debtService.getAllDetailToString(user.getId()));

                }
                return ResponseEntity.ok(new JwtResponseDTO(token, messages));
        }

        @PostMapping(value = "/logout")
        public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader,
                        @AuthenticationPrincipal UserDetails userDetails) {
                User user = userService.findUserByUsername(userDetails.getUsername());

                String jwtToken = authorizationHeader.substring(7);
                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);

                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        if (SecurityContextHolder.getContext().getAuthentication().getName().equals(username)) {
                                SecurityContextHolder.getContext().setAuthentication(null);
                        } else {
                                return new ResponseEntity<>(
                                                new JwtLogoutResponseDTO(
                                                                "Please wait, there is someone loggedin!"),
                                                HttpStatus.UNAUTHORIZED);
                        }
                }
                return ResponseEntity.ok(
                                new JwtLogoutResponseDTO(String.format("Goodbye, %s!", user.getUsername())));
        }

}
