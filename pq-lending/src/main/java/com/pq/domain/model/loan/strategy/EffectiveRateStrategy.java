package com.pq.domain.model.loan.strategy;

import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EffectiveRateStrategy implements InterestStrategy {
    @Override
    public List<Payment> generateSchedule(
            Money principal, Tenor tenor,
            double annualRate, LocalDate startDate) {

        List<Payment> payments = new ArrayList<>();
        int months = tenor.getMonths();

        BigDecimal pokok = principal.getAmount();
        BigDecimal sisaPokok = pokok;
        BigDecimal rate = BigDecimal.valueOf(annualRate);
        BigDecimal pokokPerBulan = pokok.divide(BigDecimal.valueOf(months), 0, RoundingMode.HALF_UP);

        for (int i = 1; i <= months; i++) {
            BigDecimal bungaBulanIni = sisaPokok.multiply(rate).divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
            BigDecimal totalBulanIni = pokokPerBulan.add(bungaBulanIni);

            PaymentId paymentId = new PaymentId("P-" + UUID.randomUUID().toString().substring(0, 5) + "-" + i);
            LocalDate dueDate = startDate.plusMonths(i);

            payments.add(new Payment(
                    paymentId,
                    i,
                    dueDate,
                    new Money(pokokPerBulan),
                    new Money(bungaBulanIni),
                    new Money(totalBulanIni)
            ));
            sisaPokok = sisaPokok.subtract(pokokPerBulan);
        }

        return payments;
    }
}