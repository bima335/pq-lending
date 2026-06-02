package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.PaymentStatus;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.loan.PaymentDistribution;
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
        Payment payment = findPayment(paymentId);
        verifyPayment(payment, amount);
        payment.markAsPaid();
        distributePayment(payment, lenders);
        closeIfFullyPaid();
    }

    private Payment findPayment(PaymentId paymentId) {
        return loan.getPayments().stream()
                .filter(payment -> payment.getPaymentId().getValue().equals(paymentId.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cicilan tidak ditemukan"));
    }

    private void verifyPayment(Payment payment, Money amount) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Tidak ada cicilan yang perlu dibayar");
        }

        Money expected = calculateExpectedAmount(payment);
        if (amount.getAmount().compareTo(expected.getAmount()) != 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak sesuai");
        }
    }

    private Money calculateExpectedAmount(Payment payment) {
        if (payment.getStatus() == PaymentStatus.OVERDUE) {
            return new Money(payment.getTotalAmount().getAmount().add(payment.getPenalty().getAmount()));
        }
        return payment.getTotalAmount();
    }

    private void distributePayment(Payment payment, List<Lender> lenders) {
        if (lenders == null || lenders.isEmpty()) {
            return;
        }

        new PaymentDistribution().distribute(loan.getFundings(), lenders, payment.getTotalAmount());
    }

    private void closeIfFullyPaid() {
        boolean allPaid = loan.getPayments().stream()
                .allMatch(payment -> payment.getStatus() == PaymentStatus.PAID);

        if (allPaid) {
            loan.setCurrentState(new ClosedState(loan));
        }
    }

    @Override
    public void close() {
        closeIfFullyPaid();
    }

    @Override
    public void cancel(com.pq.domain.model.borrower.Borrower borrower, java.util.List<com.pq.domain.model.lender.Lender> lenders) {
        throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
    }
}
