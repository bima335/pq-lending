package com.pq.application.usecase;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId; // Asumsi Anda sudah membuat class ini
import com.pq.domain.repository.BorrowerRepository;
import com.pq.domain.repository.LenderRepository;
import com.pq.domain.repository.LoanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepayLoanUseCase {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;

    public RepayLoanUseCase(LoanRepository loanRepository, 
                            BorrowerRepository borrowerRepository, 
                            LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.lenderRepository = lenderRepository;
    }

    public void execute(String loanIdStr, Money repaymentAmount) {
        LoanId loanId = new LoanId(loanIdStr);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));

        Borrower borrower = borrowerRepository.findById(loan.getBorrowerId())
                .orElseThrow(() -> new IllegalStateException("Data Peminjam tidak valid."));

        borrower.deductBalance(repaymentAmount);

        List<Lender> lenders = new ArrayList<>();
        for (Funding funding : loan.getFundings()) {
            Lender lender = lenderRepository.findById(funding.getLenderId())
                    .orElseThrow(() -> new IllegalStateException("Data Lender tidak ditemukan."));
            lenders.add(lender);
        }

        PaymentId paymentId = new PaymentId(UUID.randomUUID().toString());

        loan.makeRepayment(paymentId, lenders, repaymentAmount);

        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        
        for (Lender lender : lenders) {
            lenderRepository.save(lender);
        }
    }
}