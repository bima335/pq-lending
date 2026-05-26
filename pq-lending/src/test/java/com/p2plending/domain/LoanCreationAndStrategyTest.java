package com.p2plending.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanCreationAndStrategyTest {

    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan(new LoanId("L-TEST-001"), new BorrowerId("B-TEST-001"));
    }

    private Borrower createBorrower(Grade grade) {
        return new Borrower(new BorrowerId("B-TEST-001"), "Test Borrower", grade, new Money(BigDecimal.ZERO));
    }

    // BR-04: Penentuan Interest Strategy
    
    @Test
    void testGradeAGetsEffectiveRateStrategy() {
        Borrower borrower = createBorrower(Grade.A);
        loan.submit(borrower, new Money(new BigDecimal("2000000")), Tenor.THREE);
        Assertions.assertEquals("EffectiveRateStrategy", loan.getInterestStrategy().getClass().getSimpleName());
    }

    @Test
    void testGradeBGetsEffectiveRateStrategy() {
        Borrower borrower = createBorrower(Grade.B);
        loan.submit(borrower, new Money(new BigDecimal("2000000")), Tenor.THREE);
        Assertions.assertEquals("EffectiveRateStrategy", loan.getInterestStrategy().getClass().getSimpleName());
    }

    @Test
    void testGradeCGetsFlatRateStrategy() {
        Borrower borrower = createBorrower(Grade.C);
        loan.submit(borrower, new Money(new BigDecimal("2000000")), Tenor.ONE);
        Assertions.assertEquals("FlatRateStrategy", loan.getInterestStrategy().getClass().getSimpleName());
    }

    @Test
    void testGradeDGetsFlatRateStrategy() {
        Borrower borrower = createBorrower(Grade.D);
        loan.submit(borrower, new Money(new BigDecimal("2000000")), Tenor.ONE);
        Assertions.assertEquals("FlatRateStrategy", loan.getInterestStrategy().getClass().getSimpleName());
    }

    @Test
    void testStrategyCannotBeChangedAfterDetermined() {
        Borrower borrower = createBorrower(Grade.C);
        loan.submit(borrower, new Money(new BigDecimal("2000000")), Tenor.ONE);
        Assertions.assertThrows(IllegalStateException.class, () -> loan.determineStrategy(Grade.A));
    }

    // BR-05: Pembuatan Loan

    @Test
    void testLoanCreatedSuccessfullyAndStateBecomesValidated() {
        Borrower borrower = createBorrower(Grade.C);
        loan.submit(borrower, new Money(new BigDecimal("30000000")), Tenor.THREE);
        Assertions.assertNotNull(loan.getAmount());
        Assertions.assertEquals(LoanState.VALIDATED, loan.getState());
    }

    @Test
    void testLoanCreationFailsWhenAmountValidationFails() {
        Borrower borrower = createBorrower(Grade.C);
        // Grade C max amount is 50,000,000. Testing with 100,000,000
        Assertions.assertThrows(IllegalArgumentException.class, () -> 
            loan.submit(borrower, new Money(new BigDecimal("100000000")), Tenor.THREE)
        );
    }

    @Test
    void testLoanCreationFailsWhenTenorValidationFails() {
        Borrower borrower = createBorrower(Grade.D);
        // Grade D allowed tenors: ONE, THREE. Testing with TWENTY_FOUR
        Assertions.assertThrows(IllegalArgumentException.class, () -> 
            loan.submit(borrower, new Money(new BigDecimal("5000000")), Tenor.TWENTY_FOUR)
        );
    }

    // BR-06: Ketentuan Masuk Fase Funding

    @Test
    void testValidatedLoanEntersFundingPhase() {
        Borrower borrower = createBorrower(Grade.A);
        loan.submit(borrower, new Money(new BigDecimal("10000000")), Tenor.TWELVE);
        
        loan.startFunding();
        Assertions.assertEquals(LoanState.FUNDING, loan.getState());
    }

    @Test
    void testFundingDeadlineIsSetCorrectly() {
        Borrower borrower = createBorrower(Grade.A);
        loan.submit(borrower, new Money(new BigDecimal("10000000")), Tenor.TWELVE);
        
        loan.startFunding();
        Assertions.assertNotNull(loan.getFundingDeadline());
        // Simple assertion to check if it's set correctly (approx 20 days as per implementation)
        Assertions.assertEquals(LocalDate.now().plusDays(20), loan.getFundingDeadline());
    }

    @Test
    void testInitialTotalFundedIsZero() {
        Borrower borrower = createBorrower(Grade.A);
        loan.submit(borrower, new Money(new BigDecimal("10000000")), Tenor.TWELVE);
        loan.startFunding();
        
        Assertions.assertEquals(new BigDecimal("0"), loan.getTotalFunded().getAmount());
    }
}
