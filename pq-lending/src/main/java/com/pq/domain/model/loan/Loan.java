package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.strategy.InterestStrategy;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private Money amount;
    private Tenor tenor;
    private Grade grade;
    private InterestStrategy interestStrategy;
    private String strategyType;
    private LoanState state;
    private LocalDate fundingDeadline;
    private final List<Funding> fundings;
    private final List<Payment> payments;

    public Loan(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.state = LoanState.SUBMITTED;
        this.fundings = new ArrayList<>();
        this.payments = new ArrayList<>();
    }

    public void determineStrategy(Grade grade) {
        if (this.interestStrategy != null) {
            throw new IllegalStateException("Strategy has already been determined and is immutable.");
        }
        this.grade = grade;
        switch (grade.getStrategyType()) {
        case "EFFECTIVE":
            this.interestStrategy = new EffectiveRateStrategy();
            break;
        case "FLAT":
            this.interestStrategy = new FlatRateStrategy();
            break;
        default:
            throw new IllegalStateException("Unknown strategy type for grade: " + grade);
        }
        this.strategyType = this.interestStrategy.getClass().getSimpleName();
    }

    public InterestStrategy getInterestStrategy() {
        return interestStrategy;
    }

    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        if (this.state != LoanState.SUBMITTED) {
            throw new IllegalStateException("Loan can only be submitted once and is in state: " + this.state);
        }

        Grade borrowerGrade = borrower.getCreditGrade();

        if (amount == null || amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount harus lebih dari 0");
        }

        if (amount.getAmount().compareTo(borrowerGrade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }

        if (tenor == null || !borrowerGrade.getAllowedTenors().contains(tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }

        this.amount = amount;
        this.tenor = tenor;
        determineStrategy(borrowerGrade);
        this.state = LoanState.VALIDATED;
    }

    public void validate() {
        if (this.amount == null || this.amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount harus lebih dari 0");
        }
        if (this.grade == null) {
            throw new IllegalStateException("Grade borrower belum ditentukan");
        }
        if (this.amount.getAmount().compareTo(this.grade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }
        if (this.tenor == null || !this.grade.getAllowedTenors().contains(this.tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }
    }

    public void startFunding() {
        this.state = LoanState.FUNDING;
        this.fundingDeadline = LocalDate.now().plusDays(20); // Pendekatan kasar 14 hari kerja
    }

    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        if (this.fundingDeadline != null && LocalDate.now().isAfter(this.fundingDeadline)) {
            this.state = LoanState.CANCELLED;
            throw new IllegalStateException("Deadline terlewat");
        }

        if (amount.getAmount().compareTo(new java.math.BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Minimum kontribusi adalah Rp 100.000");
        }

        java.math.BigDecimal currentTotal = getTotalFunded().getAmount();
        java.math.BigDecimal targetAmount = this.amount.getAmount();
        java.math.BigDecimal remainingAmount = targetAmount.subtract(currentTotal);

        java.math.BigDecimal actualAmount = amount.getAmount();
        if (actualAmount.compareTo(remainingAmount) > 0) {
            actualAmount = remainingAmount;
        }

        double portion = actualAmount.doubleValue() / targetAmount.doubleValue();

        Funding funding = new Funding(
                new com.pq.domain.model.valueobject.FundingId("FND-" + System.nanoTime()),
                lenderId,
                new Money(actualAmount),
                portion
        );
        this.fundings.add(funding);
    }

    public void cancel(Borrower borrower, List<Lender> lenders) {
        if (this.state == LoanState.DISBURSED || this.state == LoanState.REPAYMENT || this.state == LoanState.CLOSED) {
            throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
        }

        Money totalFunded = getTotalFunded();
        if (totalFunded.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            this.state = LoanState.CANCELLED;
            return;
        }

        double percent = getFundingPercentage();
        java.math.BigDecimal rate = java.math.BigDecimal.ZERO;
        if (percent > 0 && percent <= 50) {
            rate = new java.math.BigDecimal("0.01");
        } else if (percent > 50 && percent < 100) {
            rate = new java.math.BigDecimal("0.02");
        }

        java.math.BigDecimal feeAmount = totalFunded.getAmount().multiply(rate).setScale(0,
                java.math.RoundingMode.HALF_UP);
        Money fee = new Money(feeAmount);

        if (borrower.getVirtualAccountBalance().getAmount().compareTo(feeAmount) < 0) {
            throw new IllegalStateException("Saldo tidak cukup untuk membayar denda");
        }

        if (feeAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            borrower.deductBalance(fee);
        }

        if (lenders != null) {
            for (Funding funding : fundings) {
                for (Lender lender : lenders) {
                    if (lender.getLenderId().getValue().equals(funding.getLenderId().getValue())) {
                        lender.addBalance(funding.getAmount());
                    }
                }
            }
        }

        this.state = LoanState.CANCELLED;
    }

    public void disburse() {
        if (getFundingPercentage() >= 100.0) {
            this.state = LoanState.DISBURSED;
            if (this.tenor != null && this.amount != null) {
                int months = this.tenor.getMonths();
                java.math.BigDecimal principalPerInstallment = this.amount.getAmount()
                        .divide(new java.math.BigDecimal(months), 0, java.math.RoundingMode.HALF_UP);
                for (int i = 1; i <= months; i++) {
                    PaymentId paymentId = new PaymentId("P" + i);
                    java.time.LocalDate dueDate = java.time.LocalDate.now().plusMonths(i);
                    Money principal = new Money(principalPerInstallment);
                    Money interest = new Money(java.math.BigDecimal.ZERO);
                    Money totalAmount = new Money(principalPerInstallment);
                    this.payments.add(new Payment(paymentId, i, dueDate, principal, interest, totalAmount));
                }
                this.state = LoanState.REPAYMENT;
            }
        }
    }

    public void makeRepayment(PaymentId paymentId, List<Lender> lenders) {
        // TODO: Anggota 5
    }

    public void close() {
        // TODO: Anggota 5
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setTenor(Tenor tenor) {
        this.tenor = tenor;
    }

    public void setState(LoanState state) {
        this.state = state;
    }

    // Getters
    public LoanId getLoanId() {
        return loanId;
    }

    public BorrowerId getBorrowerId() {
        return borrowerId;
    }

    public Money getAmount() {
        return amount;
    }

    public Tenor getTenor() {
        return tenor;
    }

    public Grade getGrade() {
        return grade;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public LoanState getState() {
        return state;
    }

    public LocalDate getFundingDeadline() {
        return fundingDeadline;
    }

    public List<Funding> getFundings() {
        return fundings;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public Money getTotalFunded() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (Funding funding : fundings) {
            total = total.add(funding.getAmount().getAmount());
        }
        return new Money(total);
    }

    public double getFundingPercentage() {
        if (amount == null || amount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        java.math.BigDecimal fundedAmount = getTotalFunded().getAmount();
        java.math.BigDecimal targetAmount = amount.getAmount();
        return fundedAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100.0;
    }
}