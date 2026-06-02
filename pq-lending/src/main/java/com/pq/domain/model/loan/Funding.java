package com.pq.domain.model.loan;

import com.pq.domain.model.valueobject.FundingId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.Money;
import java.time.LocalDateTime;

public class Funding {
    private final FundingId fundingId;
    private final LenderId lenderId;
    private final Money amount;
    private double portion;
    private final LocalDateTime fundedAt;

    public Funding(FundingId fundingId, LenderId lenderId,
            Money amount, double portion) {
        this.fundingId = fundingId;
        this.lenderId = lenderId;
        this.amount = amount;
        this.portion = portion;
        this.fundedAt = LocalDateTime.now();
    }

    public FundingId getFundingId() {
        return fundingId;
    }

    public LenderId getLenderId() {
        return lenderId;
    }

    public Money getAmount() {
        return amount;
    }

    public double getPortion() {
        return portion;
    }

    public LocalDateTime getFundedAt() {
    return fundedAt;
}

    public void setPortion(double portion) {
        this.portion = portion;
    }
}