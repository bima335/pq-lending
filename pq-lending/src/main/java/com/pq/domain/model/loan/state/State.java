package com.pq.domain.model.loan.state;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.LenderId;

public abstract class State {
    protected final Loan loan;

    public State(Loan loan) {
        this.loan = loan;
    }

    public abstract void submit();

    public abstract void cancel();

    public abstract void validate();

    public abstract void startFunding();

    public abstract void addFunding(LenderId lenderId, Money amount, Lender lender);

    public abstract void disburse();

    public abstract void makePayment(Payment payment);

    public abstract void complete();
}
