package com.pq.application.usecase;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.repository.BorrowerRepository;
import com.pq.domain.repository.LoanRepository;

public class DisburseLoanUseCase {
    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;

    public DisburseLoanUseCase(LoanRepository loanRepository, BorrowerRepository borrowerRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
    }

    public void execute(String loanIdStr) {
        LoanId loanId = new LoanId(loanIdStr);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));

        if (loan.getFundingPercentage() < 100.0) {
            throw new IllegalStateException("Pinjaman belum terdanai 100%");
        }

        Borrower borrower = borrowerRepository.findById(loan.getBorrowerId())
                .orElseThrow(() -> new IllegalStateException("Data Peminjam korup/tidak ditemukan."));

        loan.disburse();

        borrower.addBalance(loan.getAmount());

        loanRepository.save(loan);
        borrowerRepository.save(borrower);
    }
}