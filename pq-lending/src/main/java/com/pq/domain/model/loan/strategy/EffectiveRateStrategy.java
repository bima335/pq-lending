package com.pq.domain.model.loan.strategy;

import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.Money;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EffectiveRateStrategy implements InterestStrategy {
    @Override
    public List<Payment> generateSchedule(
            Money principal, Tenor tenor,
            double annualRate, LocalDate startDate) {
        // TODO
        return new ArrayList<>();
    }
}