package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import java.time.LocalDate;
import java.util.List;

public class DisbursedState extends State {
    public DisbursedState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.DISBURSED;
    }

    @Override
    public void cancel(com.pq.domain.model.borrower.Borrower borrower, java.util.List<com.pq.domain.model.lender.Lender> lenders) {
        throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
    }

    /** Transisi dari DISBURSED → REPAYMENT dengan generate jadwal cicilan */
    @Override
    public void disburse() {
        if (loan.getTenor() != null && loan.getAmount() != null && loan.getPayments().isEmpty()) {
            double annualRate = 0.12;
            LocalDate startDate = LocalDate.now();
            List<Payment> generatedPayments = loan.getInterestStrategy().generateSchedule(
                    loan.getAmount(), loan.getTenor(), annualRate, startDate);
            loan.getPayments().addAll(generatedPayments);
        }
        loan.setCurrentState(new RepaymentState(loan));
    }
}
