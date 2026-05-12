package com.pq.domain;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class KontribusiLenderTest {

    private Loan loan;
    private Lender lender1;
    private Lender lender2;

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        loan = new Loan(new LoanId("LOAN-1"), new BorrowerId("BORROWER-1"));
        
        // Use reflection to bypass Anggota 1 & Anggota 2 un-implemented methods
        setField(loan, "state", LoanState.FUNDING);
        setField(loan, "amount", new Money(new BigDecimal("10000000")));
        setField(loan, "fundingDeadline", LocalDate.now().plusDays(7));
        
        lender1 = new Lender(new LenderId("LENDER-1"), "John", new Money(new BigDecimal("10000000")));
        lender2 = new Lender(new LenderId("LENDER-2"), "Jane", new Money(new BigDecimal("10000000")));
    }

    @Test
    void testKontribusiDitolakJikaKurangDariMinimum() {
        Money amount = new Money(new BigDecimal("50000"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            loan.addFunding(lender1.getLenderId(), amount, lender1)
        );
        assertEquals("Minimum kontribusi adalah Rp 100.000", ex.getMessage());
    }

    @Test
    void testKontribusiDiterimaJikaMemenuhiMinimum() {
        Money amount = new Money(new BigDecimal("500000"));
        loan.addFunding(lender1.getLenderId(), amount, lender1);
        
        assertEquals(1, loan.getFundings().size());
        assertEquals(0, new BigDecimal("500000").compareTo(loan.getTotalFunded().getAmount()));
    }

    @Test
    void testKontribusiDicapJikaMelebihiSisaTarget() {
        // Terkumpul 9.5M
        loan.addFunding(lender2.getLenderId(), new Money(new BigDecimal("9500000")), lender2);
        
        // Lender mencoba mendanai 2M
        loan.addFunding(lender1.getLenderId(), new Money(new BigDecimal("2000000")), lender1);
        
        // Cek totalnya 10M, artinya kontribusi kedua hanya masuk 500k
        assertEquals(0, new BigDecimal("10000000").compareTo(loan.getTotalFunded().getAmount()));
        
        Funding secondFunding = loan.getFundings().get(1);
        assertEquals(0, new BigDecimal("500000").compareTo(secondFunding.getAmount().getAmount()));
    }

    @Test
    void testKontribusiDitolakDanCancelledJikaDeadlineTerlewat() {
        setField(loan, "fundingDeadline", LocalDate.now().minusDays(1)); // Terlewat
        
        Exception ex = assertThrows(IllegalStateException.class, () -> 
            loan.addFunding(lender1.getLenderId(), new Money(new BigDecimal("1000000")), lender1)
        );
        
        assertEquals("Deadline terlewat", ex.getMessage());
        assertEquals(LoanState.CANCELLED, loan.getState());
    }

    @Test
    void testPorsiLenderDihitungBerdasarkanKontribusi() {
        loan.addFunding(lender1.getLenderId(), new Money(new BigDecimal("3000000")), lender1);
        
        Funding funding = loan.getFundings().get(0);
        assertEquals(0.3, funding.getPortion(), 0.001);
    }

    @Test
    void testPorsiLenderDiakumulasiJikaMendanaiLebihDariSekali() {
        loan.addFunding(lender1.getLenderId(), new Money(new BigDecimal("2000000")), lender1);
        loan.addFunding(lender1.getLenderId(), new Money(new BigDecimal("1000000")), lender1);
        
        double totalPortion = loan.getFundings().stream()
            .filter(f -> f.getLenderId().equals(lender1.getLenderId()))
            .mapToDouble(Funding::getPortion)
            .sum();
            
        assertEquals(0.3, totalPortion, 0.001);
    }
}
