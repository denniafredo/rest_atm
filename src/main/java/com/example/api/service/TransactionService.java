package com.example.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.api.config.JwtTokenUtil;
import com.example.api.dao.TransactionRepository;
import com.example.api.dto.debt.CreateDebtDTO;
import com.example.api.dto.transaction.DepositLogResponseDTO;
import com.example.api.dto.transaction.ResponseDTO;
import com.example.api.model.Debt;
import com.example.api.model.DepositLog;
import com.example.api.model.Transaction;
import com.example.api.model.User;

@Service
public class TransactionService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final TransactionRepository transactionRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final DebtService debtService;
    private final DepositLogService depositLogService;

    @Autowired
    public TransactionService(
            UserService userService,
            DebtService debtService,
            TransactionRepository transactionRepository,
            JwtTokenUtil jwtTokenUtil,
            DepositLogService depositLogService) {
        this.userService = userService;
        this.debtService = debtService;
        this.transactionRepository = transactionRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.depositLogService = depositLogService;
    }

    public Double getBalance(Long id) {
        List<Transaction> transactionsBySender = transactionRepository.findBySenderId(id);
        double totalAmount = 0;
        for (Transaction transaction : transactionsBySender) {
            if (transaction.getType().equals("DEPOSIT") && transaction.getStatus().equals("PAID")) {
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
            long nextId = transactionRepository.findNextId();
            Transaction transaction = Transaction.builder()
                    .id(nextId)
                    .sender(user)
                    .amount(amount)
                    .type("DEPOSIT")
                    .status("UNPAID")
                    .build();// unpaid -> paid after kafka consume
            transactionRepository.save(transaction);
            kafkaTemplate.send("deposit", String.format("%s", nextId));
            messages.add("Processing your deposit...");
            messages.add("Please check the deposit log periodically");
        } catch (Exception e) {
            messages.add(String.format("Deposit failed!"));
            messages.add(String.format("Error: %s", e.getMessage()));
        }

        response.setMessage(messages);
        return response;
    }

    public List<DepositLogResponseDTO> depositLog(User user, String status) {
        List<DepositLogResponseDTO> logs = new ArrayList<DepositLogResponseDTO>();
        List<String> messages = new ArrayList<String>();
        try {
            List<DepositLog> depositLogs = depositLogService.findByUserIdAndStatus(user.getId(), status);
            if (depositLogs != null) {
                Map<Long, List<String>> groupedMessages = depositLogs.stream()
                        .collect(Collectors.groupingBy(DepositLog::getBatch,
                                Collectors.mapping(DepositLog::getMessage, Collectors.toList())));
                logs = groupedMessages.entrySet().stream()
                        .map(entry -> new DepositLogResponseDTO(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

                for (DepositLog depositLog : depositLogs) {
                    depositLog.setStatus("READ");
                    depositLogService.create(depositLog);
                }
            } else {
                messages.add(String.format("No new records."));
            }
        } catch (Exception e) {
            messages.add(String.format("Failed to get log!"));
            messages.add(String.format("Error: %s", e.getMessage()));
        }
        return logs;
    }

    public ResponseDTO depositProccess(Long IdTransaction) {
        ResponseDTO response = new ResponseDTO();
        List<String> messages = new ArrayList<String>();
        DepositLog depositLog = new DepositLog();
        Long batchId = depositLogService.findNextBatchId();
        String message;
        try {
            Transaction transaction = transactionRepository.findById(IdTransaction).get();
            transaction.setStatus("PAID");

            User user = transaction.getSender();

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

                        message = String.format("Transferred $%s to %s", paidAmount, debt.getOwed().getUsername());
                        messages.add(message);
                        depositLog = DepositLog.builder()
                                .id(depositLogService.findNextId())
                                .user(user)
                                .message(message)
                                .status("UNREAD")
                                .batch(batchId)
                                .build();
                        depositLogService.create(depositLog);
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

                        message = String.format("Transferred $%s to %s", balance, debt.getOwed().getUsername());
                        messages.add(message);
                        depositLog = DepositLog.builder()
                                .id(depositLogService.findNextId())
                                .user(user)
                                .message(message)
                                .status("UNREAD")
                                .batch(batchId)
                                .build();
                        depositLogService.create(depositLog);
                    }
                    balance = getBalance(user.getId());
                }
            }

            message = balanceToString(user.getId());
            messages.add(message);
            depositLog = DepositLog.builder()
                    .id(depositLogService.findNextId())
                    .user(user)
                    .message(message)
                    .status("UNREAD")
                    .batch(batchId)
                    .build();
            depositLogService.create(depositLog);

            List<String> details = debtService.getAllDetailToString(user.getId());
            messages.addAll(details);
            if (details != null) {
                for (String detail : details) {
                    depositLog = DepositLog.builder()
                            .id(depositLogService.findNextId())
                            .user(user)
                            .message(detail)
                            .status("UNREAD")
                            .batch(batchId)
                            .build();
                    depositLogService.create(depositLog);
                }
            }
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
            Debt owedDebt = debtService.findByOwesIdAndOwedId(receiver.getId(), sender.getId());
            if (owedDebt != null) { // owed from
                double owedDeptAmount = owedDebt.getAmount();
                double paidAmount = 0;
                if (amount <= owedDeptAmount) {
                    paidAmount = amount;
                    amount -= owedDeptAmount;
                } else {
                    amount -= owedDeptAmount;
                    paidAmount = owedDeptAmount;
                }
                CreateDebtDTO debtDto = CreateDebtDTO.builder()
                        .owes(receiver)
                        .owed(sender)
                        .amount(paidAmount)
                        .build();
                debtService.pay(debtDto);
            }

            if (amount > 0) {
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
