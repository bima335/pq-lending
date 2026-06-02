package com.pq.domain.model.loan.observer;

import com.pq.domain.model.loan.Loan;

public class AutoDisbursementObserver implements FundingObserver {
    @Override
    public void onFundingCompleted(Loan loan) {
        loan.disburse();
    }
}
