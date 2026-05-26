package com.pq.domain.repository;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import java.util.Optional;

public interface LoanRepository {
    Optional<Loan> findById(LoanId loanId);
    
    void save(Loan loan);
}