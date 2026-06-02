package com.pq.domain.model.loan.observer;

import com.pq.domain.model.loan.Loan;
public interface FundingObserver {
    void onFundingCompleted(Loan loan);
}
