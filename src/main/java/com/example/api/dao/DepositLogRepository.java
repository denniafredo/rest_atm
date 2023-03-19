package com.example.api.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.api.model.DepositLog;

@Repository
public interface DepositLogRepository extends JpaRepository<DepositLog, Long> {
    @Query(value = "SELECT coalesce(max(d.id),0) +1 FROM DepositLog d")
    long findNextId();

    @Query(value = "SELECT coalesce(max(d.batch),0) +1 FROM DepositLog d")
    long findNextBatchId();

    List<DepositLog> findByUserIdAndStatusOrderByBatchDesc(Long id, String status);
}
