package com.pq.domain.model.loan;

import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.RoundingMode;

public class PaymentDistribution {
    public void distribute(List<Funding> fundings, List<Lender> lenders, Money paymentAmount) {
        if (fundings == null || fundings.isEmpty() || lenders == null || lenders.isEmpty()) {
            return;
        }

        Map<String, Lender> lenderById = lenders.stream()
                .collect(Collectors.toMap(lender -> lender.getLenderId().getValue(), lender -> lender));

        BigDecimal amountValue = paymentAmount.getAmount();
        for (Funding funding : fundings) {
            Lender lender = lenderById.get(funding.getLenderId().getValue());
            if (lender == null) {
                continue;
            }

            BigDecimal lenderShare = amountValue
                    .multiply(BigDecimal.valueOf(funding.getPortion()))
                    .setScale(0, RoundingMode.HALF_UP);

            lender.addBalance(new Money(lenderShare));
        }
    }
}
