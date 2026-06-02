package com.pq.application.usecase;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.repository.LoanRepository;

public class ValidateLoanUseCase {
    private final LoanRepository loanRepository;

    public ValidateLoanUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void execute(String loanIdStr, Grade assignedGrade) {
        LoanId loanId = new LoanId(loanIdStr);
        
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));

        loan.determineInterestStrategy(assignedGrade);
        
        loan.validate();
        loan.startFunding();

        loanRepository.save(loan);
    }
}