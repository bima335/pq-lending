// src/main/java/com/pq/domain/model/lender/Lender.java
package com.pq.domain.model.lender;

import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.Money;
import java.math.BigDecimal;

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

    public LenderId getLenderId() { return lenderId; }
    public String getName() { return name; }
    public Money getVirtualAccountBalance() {
        return virtualAccountBalance;
    }
    public void addBalance(Money amount) {
        BigDecimal newBalance = this.virtualAccountBalance.getAmount()
            .add(amount.getAmount());
        this.virtualAccountBalance = new Money(newBalance);
    }
}