package com.example.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.api.config.JwtTokenUtil;
import com.example.api.dto.transaction.DepositDTO;
import com.example.api.dto.transaction.DepositLogDTO;
import com.example.api.dto.transaction.ResponseDTO;
import com.example.api.dto.transaction.TransferDTO;
import com.example.api.dto.transaction.WithdrawDTO;
import com.example.api.model.User;
import com.example.api.service.TransactionService;
import com.example.api.service.UserService;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService transactionService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Autowired
    public TransactionController(
            TransactionService transactionService,
            UserService userService,
            JwtTokenUtil jwtTokenUtil) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping(value = "/deposit")
    public ResponseEntity<ResponseDTO> deposit(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DepositDTO depositDTO) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(transactionService.deposit(user, depositDTO.getAmount()));
    }

    @PostMapping(value = "/deposit-log")
    public ResponseEntity<ResponseDTO> depositLog(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DepositLogDTO depositLogDTO) {
        if (!depositLogDTO.getStatus().equals("UNREAD") && !depositLogDTO.getStatus().equals("READ")) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findUserByUsername(userDetails
                .getUsername());
        return ResponseEntity.ok(transactionService.depositLog(user, depositLogDTO.getStatus()));
    }

    @PostMapping(value = "/withdraw")
    public ResponseEntity<ResponseDTO> withdraw(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WithdrawDTO withdrawDTO) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(transactionService.withdraw(user, withdrawDTO.getAmount()));
    }

    @PostMapping(value = "/transfer")
    public ResponseEntity<ResponseDTO> transfer(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransferDTO transferDTO) {
        User sender = userService.findUserByUsername(userDetails.getUsername());
        User receiver = userService.findUserByUsername(transferDTO.getTarget());
        return ResponseEntity.ok(transactionService.transfer(sender, receiver, transferDTO.getAmount()));
    }
}
