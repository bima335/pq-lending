package com.pq.domain.model.loan.paymentstate;

import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.enums.PaymentStatus;

public class PaidState extends PaymentState {
    
    public PaidState(Payment payment) {
        super(payment);
    }

    @Override
    public PaymentStatus getPaymentStatusEnum() {
        return PaymentStatus.PAID;
    }
}
