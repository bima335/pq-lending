package com.pq.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CancelDanDisbursementTest {

    @Test
    void cancelWithoutContributionsDoesNotChargeFee() {
        Loan loan = new Loan(new LoanId("L001"), new BorrowerId("B001"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.FUNDING);

        Borrower borrower = new Borrower(new BorrowerId("B001"), "Borrower1", Grade.A,
                new Money(BigDecimal.valueOf(1000000)));

        loan.cancel(borrower, List.of());

        Assertions.assertEquals(LoanState.CANCELLED, loan.getState());
        Assertions.assertEquals(BigDecimal.valueOf(1000000), borrower.getVirtualAccountBalance().getAmount());
    }

    @Test
    void cancelWith40PercentFundingChargesOnePercentFeeAndRefundsLender() {
        Loan loan = new Loan(new LoanId("L002"), new BorrowerId("B002"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.FUNDING);

        Lender lender = new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(5000000)));
        loan.addFunding(lender.getLenderId(), new Money(BigDecimal.valueOf(4000000)), lender);

        Borrower borrower = new Borrower(new BorrowerId("B002"), "Borrower2", Grade.A,
                new Money(BigDecimal.valueOf(1000000)));

        loan.cancel(borrower, List.of(lender));

        Assertions.assertEquals(LoanState.CANCELLED, loan.getState());
        Assertions.assertEquals(BigDecimal.valueOf(960000), borrower.getVirtualAccountBalance().getAmount());
        Assertions.assertTrue(lender.getVirtualAccountBalance().getAmount().compareTo(BigDecimal.valueOf(5000000)) > 0);
    }

    @Test
    void cancelWith70PercentFundingChargesTwoPercentFeeAndRefundsLender() {
        Loan loan = new Loan(new LoanId("L003"), new BorrowerId("B003"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.FUNDING);

        Lender lender = new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(5000000)));
        loan.addFunding(lender.getLenderId(), new Money(BigDecimal.valueOf(7000000)), lender);

        Borrower borrower = new Borrower(new BorrowerId("B003"), "Borrower3", Grade.A,
                new Money(BigDecimal.valueOf(1000000)));

        loan.cancel(borrower, List.of(lender));

        Assertions.assertEquals(LoanState.CANCELLED, loan.getState());
        Assertions.assertEquals(BigDecimal.valueOf(860000), borrower.getVirtualAccountBalance().getAmount());
        Assertions.assertTrue(lender.getVirtualAccountBalance().getAmount().compareTo(BigDecimal.valueOf(5000000)) > 0);
    }

    @Test
    void cancelRejectedWhenBorrowerBalanceInsufficient() {
        Loan loan = new Loan(new LoanId("L004"), new BorrowerId("B004"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.FUNDING);

        Lender lender = new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(5000000)));
        loan.addFunding(lender.getLenderId(), new Money(BigDecimal.valueOf(7000000)), lender);

        Borrower borrower = new Borrower(new BorrowerId("B004"), "Borrower4", Grade.A,
                new Money(BigDecimal.valueOf(0)));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> loan.cancel(borrower, List.of(lender)));

        Assertions.assertEquals("Saldo tidak cukup untuk membayar denda", exception.getMessage());
    }

    @Test
    void cancelRejectedAfterLoanDisbursed() {
        Loan loan = new Loan(new LoanId("L005"), new BorrowerId("B005"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.DISBURSED);

        Borrower borrower = new Borrower(new BorrowerId("B005"), "Borrower5", Grade.A,
                new Money(BigDecimal.valueOf(1000000)));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> loan.cancel(borrower, List.of()));

        Assertions.assertEquals("Loan tidak dapat dibatalkan setelah dana cair", exception.getMessage());
    }

    @Test
    void loanDisbursedWhenFundingReachesHundredPercent() {
        Loan loan = new Loan(new LoanId("L006"), new BorrowerId("B006"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setTenor(Tenor.THREE);
        loan.addFunding(new LenderId("L001"), new Money(BigDecimal.valueOf(9000000)),
                new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(10000000))));
        loan.addFunding(new LenderId("L002"), new Money(BigDecimal.valueOf(1000000)),
                new Lender(new LenderId("L002"), "Lender2", new Money(BigDecimal.valueOf(10000000))));

        loan.disburse();

        Assertions.assertEquals(LoanState.REPAYMENT, loan.getState());
    }

    @Test
    void loanNotDisbursedWhenFundingBelowHundredPercent() {
        Loan loan = new Loan(new LoanId("L007"), new BorrowerId("B007"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setState(LoanState.FUNDING);
        loan.addFunding(new LenderId("L001"), new Money(BigDecimal.valueOf(5000000)),
                new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(10000000))));

        loan.disburse();

        Assertions.assertEquals(LoanState.FUNDING, loan.getState());
    }

    @Test
    void createPaymentScheduleAfterDisburse() {
        Loan loan = new Loan(new LoanId("L008"), new BorrowerId("B008"));
        loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        loan.setTenor(Tenor.THREE);
        loan.addFunding(new LenderId("L001"), new Money(BigDecimal.valueOf(10000000)),
                new Lender(new LenderId("L001"), "Lender1", new Money(BigDecimal.valueOf(10000000))));

        loan.disburse();

        Assertions.assertEquals(3, loan.getPayments().size());
        Assertions.assertEquals(LoanState.REPAYMENT, loan.getState());
    }
}
