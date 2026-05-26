package com.pq.domain.model.loan;

import com.pq.domain.model.enums.PaymentStatus;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId;
import java.time.LocalDate;
import com.pq.domain.model.loan.paymentstate.PaymentState;
import com.pq.domain.model.loan.paymentstate.UnpaidState;

public class Payment {
    private final PaymentId paymentId;
    private final int installmentNumber;
    private final LocalDate dueDate;
    private LocalDate paidDate;
    private final Money principal;
    private final Money interest;
    private final Money totalAmount;
    private PaymentStatus status;
    private PaymentState currentState;

    public Payment(PaymentId paymentId,
            int installmentNumber,
            LocalDate dueDate,
            Money principal,
            Money interest,
            Money totalAmount) {
        this.paymentId = paymentId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principal = principal;
        this.interest = interest;
        this.totalAmount = totalAmount;
        this.status = PaymentStatus.UNPAID;
        this.currentState = new UnpaidState(this);
    }

    public void markAsPaid() {
        this.currentState.markAsPaid();
    }

    public void internalMarkAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidDate = LocalDate.now();
    }

    public void setCurrentState(PaymentState state) {
        this.currentState = state;
        this.status = state.getPaymentStatusEnum();
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public Money getPrincipal() {
        return principal;
    }

    public Money getInterest() {
        return interest;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}