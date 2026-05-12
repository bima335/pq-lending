package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
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

    
    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        // BR-02: Validasi Amount
        if (amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount harus lebih dari 0");
        }

        Grade borrowerGrade = borrower.getCreditGrade();

        if (amount.getAmount().compareTo(borrowerGrade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }

        // BR-03: Validasi Tenor
        if (!borrowerGrade.getAllowedTenors().contains(tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }

        // Simpan data loan jika valid
        this.amount = amount;
        this.tenor = tenor;
        this.grade = borrowerGrade;
        this.strategyType = borrowerGrade.getStrategyType();
    }

    public void validate() {
        this.state = LoanState.VALIDATED;
    }

    public void startFunding() {
        this.state = LoanState.FUNDING;
    }

    public void addFunding(LenderId lenderId,
                           Money amount,
                           Lender lender) {
        if (fundingDeadline != null && LocalDate.now().isAfter(fundingDeadline)) {
            this.state = LoanState.CANCELLED;
            throw new IllegalStateException("Deadline terlewat");
        }
        if (amount.getAmount().compareTo(new java.math.BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Minimum kontribusi adalah Rp 100.000");
        }
        
        Money sisaTarget = this.amount.subtract(getTotalFunded());
        Money acceptedAmount = amount.min(sisaTarget);
        
        double portion = acceptedAmount.getAmount().doubleValue() / this.amount.getAmount().doubleValue();
        Funding funding = new Funding(new FundingId(java.util.UUID.randomUUID().toString()), lenderId, acceptedAmount, portion);
        this.fundings.add(funding);
    }

    public void cancel(Borrower borrower,
                       List<Lender> lenders) {
        if (this.state == LoanState.DISBURSED
                || this.state == LoanState.REPAYMENT
                || this.state == LoanState.CLOSED) {
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

        java.math.BigDecimal feeAmount = totalFunded.getAmount().multiply(rate)
                .setScale(0, java.math.RoundingMode.HALF_UP);
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

    public void makeRepayment(PaymentId paymentId,
            List<Lender> lenders) {
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
        for (Funding f : fundings) {
            total = total.add(f.getAmount().getAmount());
        }
        return new Money(total);
    }

    public double getFundingPercentage() {
        if (this.amount == null || this.amount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return getTotalFunded().getAmount().doubleValue() / this.amount.getAmount().doubleValue();
    }
}