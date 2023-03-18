package com.example.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.config.JwtTokenUtil;
import com.example.api.dao.TransactionRepository;
import com.example.api.dto.debt.CreateDebtDTO;
import com.example.api.dto.transaction.ResponseDTO;
import com.example.api.model.Debt;
import com.example.api.model.Transaction;
import com.example.api.model.User;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final DebtService debtService;

    @Autowired
    public TransactionService(
            UserService userService,
            DebtService debtService,
            TransactionRepository transactionRepository,
            JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.debtService = debtService;
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
                    || transaction.getType().equals("TRANSFER")) {
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

    public ResponseDTO deposit(User user, Double amount) {
        ResponseDTO response = new ResponseDTO();
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

            // pay dept if exist
            List<Debt> debts = debtService.findByOwesId(user.getId());
            double balance = getBalance(user.getId());

            if (debts != null && balance > 0) {
                for (Debt debt : debts) {
                    if (balance >= debt.getAmount()) {
                        double paidAmount = debt.getAmount();
                        Transaction newTransaction = Transaction.builder()
                                .id(transactionRepository.findNextId())
                                .sender(user)
                                .receiver(debt.getOwed())
                                .amount(paidAmount)
                                .type("TRANSFER")
                                .status("PAID")
                                .build();
                        transactionRepository.save(newTransaction);

                        CreateDebtDTO debtDto = CreateDebtDTO.builder()
                                .owes(user)
                                .owed(debt.getOwed())
                                .amount(paidAmount)
                                .build();
                        debtService.pay(debtDto);

                        messages.add(
                                String.format("Transferred $%s to %s", paidAmount, debt.getOwed().getUsername()));
                    } else {
                        Transaction newTransaction = Transaction.builder()
                                .id(transactionRepository.findNextId())
                                .sender(user)
                                .receiver(debt.getOwed())
                                .amount(balance)
                                .type("TRANSFER")
                                .status("PAID")
                                .build();
                        transactionRepository.save(newTransaction);

                        CreateDebtDTO debtDto = CreateDebtDTO.builder()
                                .owes(user)
                                .owed(debt.getOwed())
                                .amount(balance)
                                .build();
                        debtService.pay(debtDto);

                        messages.add(
                                String.format("Transferred $%s to %s", balance, debt.getOwed().getUsername()));
                    }
                    balance = getBalance(user.getId());
                }
            }
            messages.add(balanceToString(user.getId()));
        } catch (Exception e) {
            messages.add(String.format("Deposit failed!"));
            messages.add(String.format("Error: %s", e.getMessage()));
        }

        response.setMessage(messages);
        return response;
    }

    public ResponseDTO withdraw(User user, Double amount) {
        ResponseDTO response = new ResponseDTO();
        List<String> messages = new ArrayList<String>();

        try {
            double currentBalance = getBalance(user.getId());
            if (currentBalance - amount < 0) {
                messages.add("Withdraw failed, Insufficient Balance!");
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

    public ResponseDTO transfer(User sender, User receiver, Double amount) {
        ResponseDTO response = new ResponseDTO();
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
                CreateDebtDTO debtDto = CreateDebtDTO.builder()
                        .owes(sender)
                        .owed(receiver)
                        .amount(unpaid)
                        .build();

                debtService.create(debtDto);
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

            messages.addAll(debtService.getAllDetailToString(sender.getId()));
        } catch (Exception e) {
            messages.add(String.format("Transfer failed!"));
            messages.add(e.getMessage());
        }
        response.setMessage(messages);
        return response;
    }

    public long findNextId() {
        return transactionRepository.findNextId();
    }

}
