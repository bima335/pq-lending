package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private Money amount;
    private Tenor tenor;
    private Grade grade;
    private String strategyType;
    private LoanState state;
    private LocalDate fundingDeadline;
    private final List<Funding> fundings;
    private final List<Payment> payments;

    public Loan(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.state = LoanState.SUBMITTED;
        this.fundings = new ArrayList<>();
        this.payments = new ArrayList<>();
    }

    // TODO: semua method diimplementasi
    // oleh masing-masing anggota
    public void submit(Borrower borrower,
                       Money amount,
                       Tenor tenor) {
        // TODO: Anggota 1
    }

    public void validate() {
        // TODO: Anggota 2
    }

    public void startFunding() {
        // TODO: Anggota 2
    }

    public void addFunding(LenderId lenderId,
                           Money amount,
                           Lender lender) {
        if (fundingDeadline != null && LocalDate.now().isAfter(fundingDeadline)) {
            this.state = LoanState.CANCELLED;
            throw new IllegalStateException("Deadline terlewat");
        }
        if (amount.getAmount().compareTo(new java.math.BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Minimum kontribusi adalah Rp 100.000");
        }
        
        Money sisaTarget = this.amount.subtract(getTotalFunded());
        Money acceptedAmount = amount.min(sisaTarget);
        
        double portion = acceptedAmount.getAmount().doubleValue() / this.amount.getAmount().doubleValue();
        Funding funding = new Funding(new FundingId(java.util.UUID.randomUUID().toString()), lenderId, acceptedAmount, portion);
        this.fundings.add(funding);
    }

    public void cancel(Borrower borrower,
                       List<Lender> lenders) {
        // TODO: Anggota 4
    }

    public void disburse() {
        // TODO: Anggota 4
    }

    public void makeRepayment(PaymentId paymentId,
                              List<Lender> lenders) {
        // TODO: Anggota 5
    }

    public void close() {
        // TODO: Anggota 5
    }

    // Getters
    public LoanId getLoanId() { return loanId; }
    public BorrowerId getBorrowerId() { return borrowerId; }
    public Money getAmount() { return amount; }
    public Tenor getTenor() { return tenor; }
    public Grade getGrade() { return grade; }
    public String getStrategyType() { return strategyType; }
    public LoanState getState() { return state; }
    public LocalDate getFundingDeadline() {
        return fundingDeadline;
    }
    public List<Funding> getFundings() { return fundings; }
    public List<Payment> getPayments() { return payments; }

    public Money getTotalFunded() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (Funding f : fundings) {
            total = total.add(f.getAmount().getAmount());
        }
        return new Money(total);
    }

    public double getFundingPercentage() {
        if (this.amount == null || this.amount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return getTotalFunded().getAmount().doubleValue() / this.amount.getAmount().doubleValue();
    }
}