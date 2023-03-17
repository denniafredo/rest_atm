package com.example.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.api.config.JwtTokenUtil;
import com.example.api.dto.transaction.DepositDTO;
import com.example.api.dto.transaction.DepositResponseDTO;
import com.example.api.dto.transaction.TransferDTO;
import com.example.api.dto.transaction.TransferResponseDTO;
import com.example.api.dto.transaction.WithdrawDTO;
import com.example.api.dto.transaction.WithdrawResponseDTO;
import com.example.api.model.Transaction;
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
    public ResponseEntity<DepositResponseDTO> deposit(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DepositDTO depositDTO) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(transactionService.deposit(user, depositDTO.getAmount()));
    }

    @PostMapping(value = "/withdraw")
    public ResponseEntity<WithdrawResponseDTO> withdraw(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WithdrawDTO withdrawDTO) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(transactionService.withdraw(user, withdrawDTO.getAmount()));
    }

    @PostMapping(value = "/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransferDTO transferDTO) {
        User sender = userService.findUserByUsername(userDetails.getUsername());
        User receiver = userService.findUserByUsername(transferDTO.getTarget());
        return ResponseEntity.ok(transactionService.transfer(sender, receiver, transferDTO.getAmount()));
    }
}
