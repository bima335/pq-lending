package com.pq.domain;

import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CancelDanDisbursementTest {
    
    private Loan loan;
    private Borrower borrower;
    private List<Lender> lenders;
    
    @BeforeEach
    void setUp() {
        // Setup borrower
        BorrowerId borrowerId = new BorrowerId("borrower-001");
        borrower = new Borrower(borrowerId, "John Doe", Grade.A, new Money(BigDecimal.valueOf(1000000)));
        
        // Setup loan
        LoanId loanId = new LoanId("loan-001");
        loan = new Loan(loanId, borrowerId);
        
        // Setup lenders
        lenders = new ArrayList<>();
    }
    
    // ========== BR-10: Cancel tanpa kontribusi tidak ada denda ==========
    @Test
    void testCancelTanpaKontribusiTidakAdaDenda() {
        // Given: loan status FUNDING tanpa kontribusi
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        BigDecimal saldoAwal = borrower.getVirtualAccountBalance().getAmount();
        
        // When: borrower cancel
        loan.cancel(borrower, lenders);
        
        // Then: status CANCELLED dan saldo borrower tidak berkurang
        assertEquals(LoanState.CANCELLED, loan.getState());
        assertEquals(saldoAwal, borrower.getVirtualAccountBalance().getAmount());
    }
    
    // ========== BR-10: Cancel 1-50% denda 1% ==========
    @Test
    void testCancelKontribusi1Sampai50PersenDenda1Persen() {
        // Given: loan target 10jt, funded 4jt (40%), borrower balance 1jt
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 4jt dari lender
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(4000000)), 0.4);
        loan.getFundings().add(funding1);
        
        BigDecimal saldoAwal = borrower.getVirtualAccountBalance().getAmount(); // 1jt
        
        // When: borrower cancel
        loan.cancel(borrower, lenders);
        
        // Then: denda 1% dari 4jt = 40000, saldo borrower berkurang 40000
        assertEquals(LoanState.CANCELLED, loan.getState());
        BigDecimal expectedSaldo = saldoAwal.subtract(BigDecimal.valueOf(40000));
        assertEquals(expectedSaldo, borrower.getVirtualAccountBalance().getAmount());
        
        // Lender mendapat refund penuh 4jt
        assertEquals(BigDecimal.valueOf(4000000), lender1.getVirtualAccountBalance().getAmount());
    }
    
    // ========== BR-10: Cancel 51-99% denda 2% ==========
    @Test
    void testCancelKontribusi51Sampai99PersenDenda2Persen() {
        // Given: loan target 10jt, funded 7jt (70%), borrower balance 1jt
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 7jt dari lender
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(7000000)), 0.7);
        loan.getFundings().add(funding1);
        
        BigDecimal saldoAwal = borrower.getVirtualAccountBalance().getAmount(); // 1jt
        
        // When: borrower cancel
        loan.cancel(borrower, lenders);
        
        // Then: denda 2% dari 7jt = 140000, saldo borrower berkurang 140000
        assertEquals(LoanState.CANCELLED, loan.getState());
        BigDecimal expectedSaldo = saldoAwal.subtract(BigDecimal.valueOf(140000));
        assertEquals(expectedSaldo, borrower.getVirtualAccountBalance().getAmount());
        
        // Lender mendapat refund penuh 7jt
        assertEquals(BigDecimal.valueOf(7000000), lender1.getVirtualAccountBalance().getAmount());
    }
    
    // ========== BR-10: Cancel ditolak jika saldo tidak cukup ==========
    @Test
    void testCancelDitolakJikaSaldoTidakCukupUntukDenda() {
        // Given: loan target 10jt, funded 7jt (70%), borrower balance 0
        borrower = new Borrower(new BorrowerId("borrower-002"), "Jane Doe", Grade.A, new Money(BigDecimal.ZERO));
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 7jt
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(7000000)), 0.7);
        loan.getFundings().add(funding1);
        
        // When & Then: cancel ditolak dengan exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loan.cancel(borrower, lenders);
        });
        assertEquals("Saldo tidak cukup untuk membayar denda", exception.getMessage());
    }
    
    // ========== BR-10: Cancel tidak bisa setelah DISBURSED ==========
    @Test
    void testCancelTidakBisaSetelahDisbursed() {
        // Given: loan status DISBURSED
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 100% and trigger disbursement state
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        loan.addFunding(lenderId1, new Money(BigDecimal.valueOf(10000000)), lender1);
        assertEquals(LoanState.DISBURSED, loan.getState());
        
        // When & Then: cancel ditolak
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loan.cancel(borrower, lenders);
        });
        assertEquals("Loan tidak dapat dibatalkan setelah dana cair", exception.getMessage());
    }
    
    // ========== BR-11: Loan otomatis DISBURSED saat funding 100% ==========
    @Test
    void testLoanOtomatisDisbursedSaatFunding100Persen() {
        // Given: loan target 10jt, funded 9jt
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 9jt
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(9000000)), 0.9);
        loan.getFundings().add(funding1);
        
        // When: lender mendanai 1jt (mencapai 100%)
        LenderId lenderId2 = new LenderId("lender-002");
        Lender lender2 = new Lender(lenderId2, "Lender 2", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender2);
        loan.addFunding(lenderId2, new Money(BigDecimal.valueOf(1000000)), lender2);
        
        // Then: status otomatis DISBURSED
        assertEquals(LoanState.DISBURSED, loan.getState());
    }
    
    // ========== BR-11: Loan tidak DISBURSED jika belum 100% ==========
    @Test
    void testLoanTidakDisbursedJikaBelum100Persen() {
        // Given: loan target 10jt, funded 5jt
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 5jt
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(5000000)), 0.5);
        loan.getFundings().add(funding1);
        
        // When: lender mendanai 1jt (total 6jt, masih < 100%)
        LenderId lenderId2 = new LenderId("lender-002");
        Lender lender2 = new Lender(lenderId2, "Lender 2", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender2);
        loan.addFunding(lenderId2, new Money(BigDecimal.valueOf(1000000)), lender2);
        
        // Then: status tetap FUNDING
        assertEquals(LoanState.FUNDING, loan.getState());
    }
    
    // ========== BR-11: Jadwal cicilan dibuat setelah DISBURSED ==========
    @Test
    void testJadwalCicilanDibuatSetelahDisbursed() {
        // Given: loan target 10jt, tenor 3 bulan, funded 100%
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 100%
        LenderId lenderId1 = new LenderId("lender-001");
        Lender lender1 = new Lender(lenderId1, "Lender 1", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender1);
        FundingId fundingId1 = new FundingId("funding-001");
        Funding funding1 = new Funding(fundingId1, lenderId1, new Money(BigDecimal.valueOf(10000000)), 1.0);
        loan.getFundings().add(funding1);
        
        // When: disburse
        loan.disburse();
        
        // Then: jadwal cicilan dibuat 3 (sesuai tenor 3 bulan)
        assertEquals(3, loan.getPayments().size());
        assertEquals(LoanState.REPAYMENT, loan.getState());
    }
}
