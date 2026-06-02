package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.loan.Payment;
import java.util.List;
import java.time.LocalDate;

public class FundingState extends State {
    public FundingState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.FUNDING;
    }

    @Override
    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        if (loan.getFundingDeadline() != null && LocalDate.now().isAfter(loan.getFundingDeadline())) {
            loan.setCurrentState(new CancelledState(loan));
            throw new IllegalStateException("Deadline terlewat");
        }

        if (amount.getAmount().compareTo(new java.math.BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Minimum kontribusi adalah Rp 100.000");
        }

        java.math.BigDecimal currentTotal = loan.getTotalFunded().getAmount();
        java.math.BigDecimal targetAmount = loan.getAmount().getAmount();
        java.math.BigDecimal remainingAmount = targetAmount.subtract(currentTotal);

        java.math.BigDecimal actualAmount = amount.getAmount();
        if (actualAmount.compareTo(remainingAmount) > 0) {
            actualAmount = remainingAmount;
        }

        double portion = actualAmount.doubleValue() / targetAmount.doubleValue();

        Funding funding = new Funding(new com.pq.domain.model.valueobject.FundingId("FND-" + System.nanoTime()),
                lenderId, new Money(actualAmount), portion);
        loan.getFundings().add(funding);
        if (loan.getFundingPercentage() >= 100.0) {
            loan.notifyFundingObservers();
        }
    }

    @Override
    public void disburse() {
        if (loan.getFundingPercentage() >= 100.0) {
            if (loan.getTenor() != null && loan.getAmount() != null) {
                int months = loan.getTenor().getMonths();
                java.math.BigDecimal principalPerInstallment = loan.getAmount().getAmount()
                        .divide(new java.math.BigDecimal(months), 0, java.math.RoundingMode.HALF_UP);
                double annualRate = loan.getGrade().getAnnualRate(); 
                LocalDate startDate = LocalDate.now();

                List<Payment> generatedPayments = loan.getInterestStrategy().generateSchedule(loan.getAmount(), loan.getTenor(),
                        annualRate, startDate);
                loan.getPayments().addAll(generatedPayments);
                loan.setCurrentState(new RepaymentState(loan));
            }
        }
    }
    
    @Override
    public void cancel(Borrower borrower, List<Lender> lenders) {
        performCancel(borrower, lenders);
    }
}
