package com.pq.application.usecase;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.repository.BorrowerRepository;
import com.pq.domain.repository.LoanRepository;

import java.util.UUID;

public class SubmitLoanUseCase {
    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;

    public SubmitLoanUseCase(BorrowerRepository borrowerRepository, LoanRepository loanRepository) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
    }

    public String execute(String borrowerIdStr, Money amount, Tenor tenor) {
        BorrowerId borrowerId = new BorrowerId(borrowerIdStr);
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower tidak ditemukan."));

        LoanId loanId = new LoanId(UUID.randomUUID().toString());
        Loan loan = new Loan(loanId, borrowerId);

        loan.submit(borrower, amount, tenor);

        loanRepository.save(loan);

        return loanId.getValue();
    }
}