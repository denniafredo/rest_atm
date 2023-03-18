package com.example.api.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.api.model.Debt;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    @Query(value = "SELECT coalesce(max(d.id),0) +1 FROM Debt d")
    long findNextId();

    List<Debt> findByOwesIdAndAmountGreaterThan(Long owesId, Double amount);

    List<Debt> findByOwedIdAndAmountGreaterThan(Long owedId, Double amount);

    Debt findByOwesIdAndOwedIdAndAmountGreaterThan(Long owesId, Long owedId, Double amount);
}
