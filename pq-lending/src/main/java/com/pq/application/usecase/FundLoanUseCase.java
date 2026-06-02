package com.pq.application.usecase;

import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.repository.LenderRepository;
import com.pq.domain.repository.LoanRepository;

public class FundLoanUseCase {
    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;

    public FundLoanUseCase(LoanRepository loanRepository, LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
    }

    public void execute(String loanIdStr, String lenderIdStr, Money amountToFund) {
        LoanId loanId = new LoanId(loanIdStr);
        LenderId lenderId = new LenderId(lenderIdStr);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));
        Lender lender = lenderRepository.findById(lenderId)
                .orElseThrow(() -> new IllegalArgumentException("Lender tidak ditemukan."));

        lender.deductBalance(amountToFund); 

        loan.addFunding(lenderId, amountToFund, lender);

        lenderRepository.save(lender);
        loanRepository.save(loan);
    }
}