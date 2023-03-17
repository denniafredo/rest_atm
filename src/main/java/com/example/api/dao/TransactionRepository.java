package com.example.api.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.api.model.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query(value = "select coalesce(max(t.id),0) +1 from Transaction t")
    long findNextId();

    List<Transaction> findBySenderId(Long sender_id);

    List<Transaction> findByReceiverIdAndTypeAndStatus(Long receiver_id, String type, String status);

}
