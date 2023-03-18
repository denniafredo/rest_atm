package com.example.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.dao.DebtRepository;
import com.example.api.dto.debt.CreateDebtDTO;
import com.example.api.model.Debt;

import static org.hibernate.internal.CoreLogging.logger;

import java.util.List;

@Service
public class DebtService {
    private final DebtRepository debtRepository;

    @Autowired
    public DebtService(
            DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    public String create(CreateDebtDTO debtDTO) {
        try {
            Debt debt = findByOwesIdAndOwedId(debtDTO.getOwes().getId(), debtDTO.getOwed().getId());
            if (debt == null) {
                debt = Debt.builder()
                        .id(findNextId())
                        .owes(debtDTO.getOwes())
                        .owed(debtDTO.getOwed())
                        .amount(debtDTO.getAmount())
                        .build();
            } else {
                debt.setAmount(debt.getAmount() + debtDTO.getAmount());
            }
            debtRepository.save(debt);

        } catch (Exception e) {
            logger(e.getMessage());
            return String.format("Failed create debt");
        }

        return String.format("Success create debt");
    }

    public List<Debt> findByOwesId(Long owesId) {
        return debtRepository.findByOwesId(owesId);
    }

    public List<Debt> findByOwedId(Long owedId) {
        return debtRepository.findByOwedId(owedId);
    }

    public Debt findByOwesIdAndOwedId(Long owesId, Long owedId) {
        return debtRepository.findByOwesIdAndOwedId(owesId, owedId);
    }

    public long findNextId() {
        return debtRepository.findNextId();
    }
}
