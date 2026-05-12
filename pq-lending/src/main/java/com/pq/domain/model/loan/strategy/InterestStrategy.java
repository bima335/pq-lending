package com.pq.domain.model.loan.strategy;

import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import java.util.List;

public interface InterestStrategy {
    List<Payment> generateSchedule(
        Money principal,
        Tenor tenor,
        double annualRate,
        java.time.LocalDate startDate
    );
}