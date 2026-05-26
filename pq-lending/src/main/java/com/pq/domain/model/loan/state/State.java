package com.pq.domain.model.loan.state;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.valueobject.PaymentId;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.enums.LoanState;

import java.util.List;

public abstract class State {
    protected final Loan loan;

    public State(Loan loan) {
        this.loan = loan;
    }

    public abstract LoanState getLoanStateEnum();

    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        throw new IllegalStateException("Tidak bisa submit loan dalam keadaan " + getLoanStateEnum());
    }

    public void validate() {
        throw new IllegalStateException("Tidak bisa validate loan dalam keadaan " + getLoanStateEnum());
    }

    public void startFunding() {
        throw new IllegalStateException("Tidak bisa start funding dalam keadaan " + getLoanStateEnum());
    }

    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        throw new IllegalStateException("Tidak bisa add funding dalam keadaan " + getLoanStateEnum());
    }

    public void cancel(Borrower borrower, List<Lender> lenders) {
        throw new IllegalStateException("Tidak bisa cancel loan dalam keadaan " + getLoanStateEnum());
    }

    public void disburse() {
        throw new IllegalStateException("Tidak bisa disburse loan dalam keadaan " + getLoanStateEnum());
    }

    public void makeRepayment(PaymentId paymentId, List<Lender> lenders, Money amount) {
        throw new IllegalStateException("Tidak bisa make repayment dalam keadaan " + getLoanStateEnum());
    }

    public void close() {
        throw new IllegalStateException("Tidak bisa close loan dalam keadaan " + getLoanStateEnum());
    }

    protected void performCancel(Borrower borrower, List<Lender> lenders) {
        Money totalFunded = loan.getTotalFunded();
        if (totalFunded.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            loan.setCurrentState(new CancelledState(loan));
            return;
        }

        double percent = loan.getFundingPercentage();
        java.math.BigDecimal rate = java.math.BigDecimal.ZERO;
        if (percent > 0 && percent <= 50) {
            rate = new java.math.BigDecimal("0.01");
        } else if (percent > 50 && percent < 100) {
            rate = new java.math.BigDecimal("0.02");
        }

        java.math.BigDecimal feeAmount = totalFunded.getAmount().multiply(rate).setScale(0,
                java.math.RoundingMode.HALF_UP);
        Money fee = new Money(feeAmount);

        if (borrower.getVirtualAccountBalance().getAmount().compareTo(feeAmount) < 0) {
            throw new IllegalStateException("Saldo tidak cukup untuk membayar denda");
        }

        if (feeAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            borrower.deductBalance(fee);
        }

        if (lenders != null) {
            for (com.pq.domain.model.loan.Funding funding : loan.getFundings()) {
                for (Lender lender : lenders) {
                    if (lender.getLenderId().getValue().equals(funding.getLenderId().getValue())) {
                        lender.addBalance(funding.getAmount());
                    }
                }
            }
        }

        loan.setCurrentState(new CancelledState(loan));
    }
}
