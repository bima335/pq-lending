package com.pq.bdd.steps;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.Money;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared context untuk Cucumber PicoContainer DI.
 * Semua step class yang menerima SharedTestContext via constructor
 * akan mendapatkan instance yang SAMA dalam satu skenario.
 */
public class SharedTestContext {

    private Borrower borrower;

    public Borrower getBorrower() {
        return borrower;
    }

    public void initBorrower(String gradeStr) {
        Grade grade = Grade.valueOf(gradeStr.toUpperCase());
        Money balance = new Money(BigDecimal.valueOf(100000000L));
        this.borrower = mock(Borrower.class);
        when(this.borrower.getBorrowerId()).thenReturn(new BorrowerId("BORROWER-" + System.nanoTime()));
        when(this.borrower.getCreditGrade()).thenReturn(grade);
        when(this.borrower.getVirtualAccountBalance()).thenReturn(balance);
    }
}
