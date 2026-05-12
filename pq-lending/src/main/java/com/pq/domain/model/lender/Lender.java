package com.pq.domain.model.lender;

import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.Money;

public class Lender {
    private final LenderId lenderId;
    private final String name;
    private Money virtualAccountBalance;

    public Lender(LenderId lenderId, String name,
                  Money virtualAccountBalance) {
        this.lenderId = lenderId;
        this.name = name;
        this.virtualAccountBalance = virtualAccountBalance;
    }

    // Dipakai saat menerima refund atau distribusi cicilan
    public void addBalance(Money amount) {
        this.virtualAccountBalance = virtualAccountBalance.add(amount);
    }

    public LenderId getLenderId() { return lenderId; }
    public String getName() { return name; }
    public Money getVirtualAccountBalance() { return virtualAccountBalance; }
}