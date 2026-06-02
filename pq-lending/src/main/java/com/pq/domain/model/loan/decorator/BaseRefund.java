package com.pq.domain.model.loan.decorator;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.Money;

public class BaseRefund implements RefundOperation {

    @Override
    public Money calculateRefundAmount(Loan loan) {
        return loan.getTotalFunded();
    }
}
