package com.example.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.config.JwtTokenUtil;
import com.example.api.dao.TransactionRepository;
import com.example.api.dto.transaction.DepositResponseDTO;
import com.example.api.dto.transaction.TransferResponseDTO;
import com.example.api.dto.transaction.WithdrawDTO;
import com.example.api.dto.transaction.WithdrawResponseDTO;
import com.example.api.model.Transaction;
import com.example.api.model.User;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Autowired
    public TransactionService(
            UserService userService,
            TransactionRepository transactionRepository,
            JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.transactionRepository = transactionRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public Double getBalance(Long id) {
        List<Transaction> transactionsBySender = transactionRepository.findBySenderId(id);
        double totalAmount = 0;
        for (Transaction transaction : transactionsBySender) {
            if (transaction.getType().equals("DEPOSIT")) {
                totalAmount += transaction.getAmount();
            } else if (transaction.getType().equals("WITHDRAW")
                    || (transaction.getType().equals("TRANSFER") && transaction.getStatus().equals("PAID"))) {
                totalAmount -= transaction.getAmount();
            }
        }
        List<Transaction> transactionsByReceiver = transactionRepository.findByReceiverIdAndTypeAndStatus(id,
                "TRANSFER", "PAID");
        for (Transaction transaction : transactionsByReceiver) {
            totalAmount += transaction.getAmount();
        }
        return totalAmount;
    }

    public String balanceToString(Long id) {
        return String.format("Your balance is $%s", getBalance(id));
    }

    public DepositResponseDTO deposit(User user, Double amount) {
        DepositResponseDTO response = new DepositResponseDTO();
        List<String> messages = new ArrayList<String>();
        try {
            Transaction transaction = Transaction.builder()
                    .id(transactionRepository.findNextId())
                    .sender(user)
                    .amount(amount)
                    .type("DEPOSIT")
                    .status("PAID")
                    .build();// unpaid -> paid on kafka
            transactionRepository.save(transaction);
            messages.add(balanceToString(user.getId()));
        } catch (Exception e) {
            messages.add(String.format("Deposit failed!"));
            messages.add(String.format("Error: %s", e.getMessage()));
        }

        response.setMessage(messages);
        return response;
    }

    public WithdrawResponseDTO withdraw(User user, Double amount) {
        WithdrawResponseDTO response = new WithdrawResponseDTO();
        List<String> messages = new ArrayList<String>();

        try {
            double currentBalance = getBalance(user.getId());
            if (currentBalance - amount < 0) {
                messages.add("Withdraw failed!");
                messages.add("Insufficient Balance!");
            } else {
                Transaction transaction = Transaction.builder()
                        .id(transactionRepository.findNextId())
                        .sender(user)
                        .amount(amount)
                        .type("WITHDRAW")
                        .status("PAID")
                        .build();

                transactionRepository.save(transaction);

                messages.add(String.format("$%s Withdrawed Successfully", amount));

            }

            messages.add(balanceToString(user.getId()));
        } catch (Exception e) {
            messages.add(String.format("Withdraw failed!"));
            messages.add(String.format("Error: %s", e.getMessage()));
        }

        response.setMessage(messages);
        return response;
    }

    public TransferResponseDTO transfer(User sender, User receiver, Double amount) {
        TransferResponseDTO response = new TransferResponseDTO();
        List<String> messages = new ArrayList<String>();

        if (sender.getUsername().equals(receiver.getUsername())) {
            messages.add(String.format("Can not transfer yourself!"));
            response.setMessage(messages);
            return response;
        }
        try {
            double senderBalance = getBalance(sender.getId());
            if (senderBalance < amount) {
                if (senderBalance > 0.0) {
                    Transaction transaction = Transaction.builder()
                            .id(transactionRepository.findNextId())
                            .sender(sender)
                            .receiver(receiver)
                            .amount(senderBalance)
                            .type("TRANSFER")
                            .status("PAID")
                            .build();

                    transactionRepository.save(transaction);
                    messages.add(String.format("Transferred $%s to %s", senderBalance, receiver.getUsername()));
                }
                double unpaid = amount - senderBalance;
                Transaction transaction = Transaction.builder()
                        .id(transactionRepository.findNextId())
                        .sender(sender)
                        .receiver(receiver)
                        .amount(unpaid)
                        .type("TRANSFER")
                        .status("UNPAID")
                        .build();

                transactionRepository.save(transaction);
            } else {
                Transaction transaction = Transaction.builder()
                        .id(transactionRepository.findNextId())
                        .sender(sender)
                        .receiver(receiver)
                        .amount(amount)
                        .type("TRANSFER")
                        .status("PAID")
                        .build();

                transactionRepository.save(transaction);
                messages.add(String.format("Transferred $%s to %s", amount, receiver.getUsername()));
            }
            messages.add(balanceToString(sender.getId()));

        } catch (Exception e) {
            messages.add(String.format("Transfer failed!"));
        }
        response.setMessage(messages);
        return response;
    }

    public long findNextId() {
        return transactionRepository.findNextId();
    }

}
