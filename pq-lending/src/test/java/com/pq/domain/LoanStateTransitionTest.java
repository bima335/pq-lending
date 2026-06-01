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
import com.pq.domain.model.valueobject.PaymentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoanStateTransitionTest {

    private Loan loan;
    private Borrower borrowerMock;
    private Lender lenderMock;
    private Money money;
    private Tenor tenor;

    @BeforeEach
    void setUp() {
        borrowerMock = mock(Borrower.class);
        when(borrowerMock.getCreditGrade()).thenReturn(Grade.A);
        when(borrowerMock.getVirtualAccountBalance()).thenReturn(new Money(new BigDecimal("10000000")));

        lenderMock = mock(Lender.class);
        money = new Money(new BigDecimal("1000000"));
        tenor = Tenor.SIX;

        loan = new Loan(new LoanId("L-001"), new BorrowerId("B-001"));
    }

    private void assertInvalidTransition(Executable action) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, action,
                "Expected IllegalStateException for invalid state transition");
        assertTrue(ex.getMessage().toLowerCase().contains("tidak bisa") ||
                ex.getMessage().toLowerCase().contains("dalam keadaan") ||
                ex.getMessage().toLowerCase().contains("invalid") ||
                ex.getMessage().toLowerCase().contains("tidak dapat"),
                "Exception message should mention invalid state transition: " + ex.getMessage());
    }

    @Test
    void testSubmittedState_Strictness() {
        loan.setState(LoanState.SUBMITTED);

        // Allowed: submit, validate
        // Forbidden: startFunding, addFunding, cancel, disburse, makeRepayment, close

        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.cancel(borrowerMock, Collections.emptyList()));
        assertInvalidTransition(() -> loan.disburse());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }

    @Test
    void testValidatedState_Strictness() {
        loan.setState(LoanState.VALIDATED);

        // Allowed: startFunding, validate, cancel
        // Forbidden: submit, addFunding, disburse, makeRepayment, close
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.disburse());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }

    @Test
    void testFundingState_Strictness() {
        loan.setState(LoanState.FUNDING);

        // Allowed: addFunding, cancel, disburse
        // Forbidden: submit, validate, startFunding, makeRepayment, close
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.validate());
        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }

    @Test
    void testCancelledState_Strictness() {
        loan.setState(LoanState.CANCELLED);

        // Allowed: none
        // Forbidden: submit, validate, startFunding, addFunding, cancel, disburse,
        // makeRepayment, close
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.validate());
        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.cancel(borrowerMock, Collections.emptyList()));
        assertInvalidTransition(() -> loan.disburse());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }

    @Test
    void testDisbursedState_Strictness() {
        loan.setState(LoanState.DISBURSED);

        // Allowed: none
        // Forbidden: submit, validate, startFunding, addFunding, cancel, disburse, makeRepayment, close
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.validate());
        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.cancel(borrowerMock, Collections.emptyList()));
        assertInvalidTransition(() -> loan.disburse());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }

    @Test
    void testRepaymentState_Strictness() {
        loan.setState(LoanState.REPAYMENT);

        // Allowed: makeRepayment, close
        // Forbidden: submit, validate, startFunding, addFunding, cancel, disburse
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.validate());
        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.cancel(borrowerMock, Collections.emptyList()));
        assertInvalidTransition(() -> loan.disburse());
    }

    @Test
    void testClosedState_Strictness() {
        loan.setState(LoanState.CLOSED);

        // Allowed: none
        // Forbidden: submit, validate, startFunding, addFunding, cancel, disburse,
        // makeRepayment, close
        assertInvalidTransition(() -> loan.submit(borrowerMock, money, tenor));
        assertInvalidTransition(() -> loan.validate());
        assertInvalidTransition(() -> loan.startFunding());
        assertInvalidTransition(() -> loan.addFunding(new LenderId("L-1"), money, lenderMock));
        assertInvalidTransition(() -> loan.cancel(borrowerMock, Collections.emptyList()));
        assertInvalidTransition(() -> loan.disburse());
        assertInvalidTransition(() -> loan.makeRepayment(new PaymentId("P-1"), Collections.emptyList(), money));
        assertInvalidTransition(() -> loan.close());
    }
}
