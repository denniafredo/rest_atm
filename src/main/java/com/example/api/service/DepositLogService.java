package com.example.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.dao.DepositLogRepository;
import com.example.api.model.DepositLog;

import static org.hibernate.internal.CoreLogging.logger;

import java.util.List;

@Service
public class DepositLogService {
    private final DepositLogRepository depositLogRepository;

    @Autowired
    public DepositLogService(
            DepositLogRepository depositLogRepository) {
        this.depositLogRepository = depositLogRepository;
    }

    public String create(DepositLog depositLog) {
        try {
            depositLogRepository.save(depositLog);
        } catch (Exception e) {
            logger(e.getMessage());
            return String.format("Failed create depositLog");
        }
        return String.format("Success create depositLog");
    }

    public List<DepositLog> findByUserIdAndStatus(Long id, String status) {
        return depositLogRepository.findByUserIdAndStatusOrderByBatchDesc(id, status);
    }

    public long findNextId() {
        return depositLogRepository.findNextId();
    }

    public long findNextBatchId() {
        return depositLogRepository.findNextBatchId();
    }
}
