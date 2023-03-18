package com.example.api.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.api.dto.transaction.ResponseDTO;
import com.example.api.service.TransactionService;

@Component
public class KafkaConsumer {
  @Autowired
  private TransactionService transactionService;

  @KafkaListener(topics = "deposit", groupId = "group1")
  public ResponseDTO process(String message) {
    long idTransaction = Long.parseLong(message);
    return transactionService.depositProccess(idTransaction);
  }

}