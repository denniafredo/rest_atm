package com.example.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.dao.DebtRepository;
import com.example.api.dto.debt.CreateDebtDTO;
import com.example.api.model.Debt;

import static org.hibernate.internal.CoreLogging.logger;

import java.util.ArrayList;
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

    public String pay(CreateDebtDTO debtDTO) {
        try {
            Debt debt = findByOwesIdAndOwedId(debtDTO.getOwes().getId(), debtDTO.getOwed().getId());
            debt.setAmount(debt.getAmount() - debtDTO.getAmount());
            debtRepository.save(debt);
            System.out.println(debt.getAmount());
        } catch (Exception e) {
            logger(e.getMessage());
            return String.format("Failed pay debt");
        }

        return String.format("Success pay debt");
    }

    public List<Debt> findByOwesId(Long owesId) {
        return debtRepository.findByOwesIdAndAmountGreaterThan(owesId, 0.0);
    }

    public List<Debt> findByOwedId(Long owedId) {
        return debtRepository.findByOwedIdAndAmountGreaterThan(owedId, 0.0);
    }

    public Debt findByOwesIdAndOwedId(Long owesId, Long owedId) {
        return debtRepository.findByOwesIdAndOwedIdAndAmountGreaterThan(owesId, owedId, 0.0);
    }

    public List<String> getOwesDetailToString(Long owesId) {
        List<String> messages = new ArrayList<String>();
        List<Debt> debts = findByOwesId(owesId);
        for (Debt debt : debts) {
            messages.add(String.format("Owed $%s to %s", debt.getAmount(), debt.getOwed().getUsername()));
        }
        return messages;
    }

    public List<String> getOwedDetailToString(Long owesId) {
        List<String> messages = new ArrayList<String>();
        List<Debt> debts = findByOwedId(owesId);
        for (Debt debt : debts) {
            messages.add(String.format("Owed $%s from %s", debt.getAmount(), debt.getOwes().getUsername()));
        }
        return messages;
    }

    public List<String> getAllDetailToString(Long owesId) {
        List<String> messages = new ArrayList<String>();
        messages.addAll(getOwesDetailToString(owesId));
        messages.addAll(getOwedDetailToString(owesId));
        return messages;
    }

    public long findNextId() {
        return debtRepository.findNextId();
    }
}
