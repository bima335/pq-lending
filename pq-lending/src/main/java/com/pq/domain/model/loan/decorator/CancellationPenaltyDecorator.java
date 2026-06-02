package com.pq.domain.model.loan.decorator;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CancellationPenaltyDecorator extends RefundDecorator {

    public CancellationPenaltyDecorator(RefundOperation wrapped) {
        super(wrapped);
    }

    @Override
    public Money calculateRefundAmount(Loan loan) {
        return wrapped.calculateRefundAmount(loan);
    }

    public Money calculatePenaltyFee(Loan loan) {
        double fundingPercentage = loan.getFundingPercentage();
        Money totalFunded = loan.getTotalFunded();

        BigDecimal rate = BigDecimal.ZERO;

        if (fundingPercentage > 0 && fundingPercentage <= 50) {
            rate = new BigDecimal("0.01");
        } else if (fundingPercentage > 50 && fundingPercentage < 100) {
            rate = new BigDecimal("0.02");
        }

        BigDecimal feeAmount = totalFunded.getAmount()
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP);

        return new Money(feeAmount);
    }
}
