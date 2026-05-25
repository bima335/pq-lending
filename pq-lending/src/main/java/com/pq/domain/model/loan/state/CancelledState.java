package com.pq.domain.model.loan.state;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.LenderId;

public class CancelledState extends State {

    public CancelledState(Loan loan) {
        super(loan);
    }

    @Override
    public void submit() {
        throw new IllegalStateException("Tidak bisa submit loan dalam keadaan Cancelled");
    }

    @Override
    public void cancel() {
        throw new IllegalStateException("Tidak bisa cancel loan dalam keadaan Cancelled");
    }

    @Override
    public void validate() {
        throw new IllegalStateException("Tidak bisa validate loan dalam keadaan Cancelled");
    }

    @Override
    public void startFunding() {
        throw new IllegalStateException("Tidak bisa start funding dalam keadaan Cancelled");
    }

    @Override
    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        throw new IllegalStateException("Tidak bisa add funding dalam keadaan Cancelled");
    }

    @Override
    public void disburse() {
        throw new IllegalStateException("Tidak bisa disburse loan dalam keadaan Cancelled");
    }

    @Override
    public void makePayment(Payment payment) {
        throw new IllegalStateException("Tidak bisa make payment dalam keadaan Cancelled");
    }

    @Override
    public void complete() {
        throw new IllegalStateException("Tidak bisa complete loan dalam keadaan Cancelled");
    }
}
