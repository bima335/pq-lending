package com.pq.domain.model.loan.paymentstate;

import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.enums.PaymentStatus;

public abstract class PaymentState {
    protected final Payment payment;

    protected PaymentState(Payment payment) {
        this.payment = payment;
    }

    public abstract PaymentStatus getPaymentStatusEnum();

    public void markAsPaid() {
        throw new IllegalStateException("Tidak bisa melakukan pembayaran dalam state " + getPaymentStatusEnum());
    }
}
