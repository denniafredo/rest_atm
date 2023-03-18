package com.example.api.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

  @KafkaListener(topics = "deposit", groupId = "group1")
  public void listen(String message) {
    System.out.println("Received message: " + message);
  }

}