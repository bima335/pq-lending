package com.pq.domain.model.loan.decorator;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.Money;

public interface RefundOperation {
    Money calculateRefundAmount(Loan loan);
}
