package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.PaymentId;
import com.pq.domain.model.valueobject.Money;
import java.util.List;

public class RepaymentState extends State {
    public RepaymentState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.REPAYMENT;
    }

    @Override
    public void makeRepayment(PaymentId paymentId, List<Lender> lenders, Money amount) {
        Payment targetPayment = null;
        for (Payment payment : loan.getPayments()) {
            if (payment.getPaymentId().getValue().equals(paymentId.getValue())) {
                targetPayment = payment;
                break;
            }
        }

        if (targetPayment == null) {
            throw new IllegalArgumentException("Cicilan tidak ditemukan");
        }

        if (amount.getAmount().compareTo(targetPayment.getTotalAmount().getAmount()) != 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak sesuai");
        }

        if (targetPayment.getStatus() == com.pq.domain.model.enums.PaymentStatus.PAID) {
            throw new IllegalStateException("Tidak ada cicilan yang perlu dibayar");
        }

        targetPayment.markAsPaid();

        java.math.BigDecimal amountToDistribute = targetPayment.getTotalAmount().getAmount();

        if (lenders != null) {
            for (Funding funding : loan.getFundings()) {
                for (Lender lender : lenders) {
                    if (lender.getLenderId().getValue().equals(funding.getLenderId().getValue())) {
                        java.math.BigDecimal portionValue = java.math.BigDecimal.valueOf(funding.getPortion());
                        java.math.BigDecimal lenderShare = amountToDistribute.multiply(portionValue).setScale(0,
                                java.math.RoundingMode.HALF_UP);

                        lender.addBalance(new Money(lenderShare));
                    }
                }
            }
        }

        close(); // Try to close if all paid
    }

    @Override
    public void close() {
        boolean allPaid = true;
        for (Payment payment : loan.getPayments()) {
            if (payment.getStatus() == com.pq.domain.model.enums.PaymentStatus.UNPAID) {
                allPaid = false;
                break;
            }
        }

        if (allPaid) {
            loan.setCurrentState(new ClosedState(loan));
        }
    }

    @Override
    public void cancel(com.pq.domain.model.borrower.Borrower borrower, java.util.List<com.pq.domain.model.lender.Lender> lenders) {
        throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
    }
}
