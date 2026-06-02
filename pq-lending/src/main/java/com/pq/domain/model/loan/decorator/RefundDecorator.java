package com.pq.domain.model.loan.decorator;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.Money;

public abstract class RefundDecorator implements RefundOperation {

    protected final RefundOperation wrapped;

    protected RefundDecorator(RefundOperation wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Money calculateRefundAmount(Loan loan) {
        return wrapped.calculateRefundAmount(loan);
    }
}
