package com.pq.domain.model.loan.paymentstate;

import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.enums.PaymentStatus;

public class OverdueState extends PaymentState {

    public OverdueState(Payment payment) {
        super(payment);
    }

    @Override
    public PaymentStatus getPaymentStatusEnum() {
        return PaymentStatus.OVERDUE;
    }

    @Override
    public void markAsPaid() {
        payment.internalMarkAsPaid();
        payment.setCurrentState(new PaidState(payment));
    }
}
