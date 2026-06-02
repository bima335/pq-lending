package com.pq.infrastructure.repository;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.repository.LoanRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLoanRepository implements LoanRepository {
    
    private final Map<String, Loan> store = new HashMap<>();

    @Override
    public Optional<Loan> findById(LoanId loanId) {
        return Optional.ofNullable(store.get(loanId.getValue()));
    }

    @Override
    public void save(Loan loan) {
        store.put(loan.getLoanId().getValue(), loan);
    }
}