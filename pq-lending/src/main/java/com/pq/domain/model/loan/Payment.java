package com.pq.domain.model.loan;

import com.pq.domain.model.enums.PaymentStatus;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.pq.domain.model.loan.paymentstate.PaymentState;
import com.pq.domain.model.loan.paymentstate.UnpaidState;
import com.pq.domain.model.loan.paymentstate.OverdueState;

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
    private Money accumulatedPenalty;

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
        this.accumulatedPenalty = new Money(BigDecimal.ZERO);
    }

    public void markAsPaid() {
        this.currentState.markAsPaid();
    }

    public void internalMarkAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidDate = LocalDate.now();
    }

    public void checkOverdue(LocalDate today) {
        checkOverdue(today, null);
    }

    public void checkOverdue(LocalDate today, Money maxPenalty) {
        long daysPastDue = ChronoUnit.DAYS.between(dueDate, today);
        if (daysPastDue > 3) {
            BigDecimal penalty = BigDecimal.valueOf(0.001)
                    .multiply(principal.getAmount())
                    .multiply(BigDecimal.valueOf(daysPastDue -3))
                    .setScale(0, RoundingMode.HALF_UP);

            if (maxPenalty != null && penalty.compareTo(maxPenalty.getAmount()) > 0) {
                penalty = maxPenalty.getAmount();
            }

            this.accumulatedPenalty = new Money(penalty);
            this.currentState = new OverdueState(this);
            this.status = PaymentStatus.OVERDUE;
        }
    }

    public Money getPenalty() {
        return accumulatedPenalty;
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