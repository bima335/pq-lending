package com.pq.domain.model.loan.paymentstate;

import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.enums.PaymentStatus;

public class UnpaidState extends PaymentState {
    
    public UnpaidState(Payment payment) {
        super(payment);
    }

    @Override
    public PaymentStatus getPaymentStatusEnum() {
        return PaymentStatus.UNPAID;
    }

    @Override
    public void markAsPaid() {
        payment.internalMarkAsPaid();
        payment.setCurrentState(new PaidState(payment));
    }
}
