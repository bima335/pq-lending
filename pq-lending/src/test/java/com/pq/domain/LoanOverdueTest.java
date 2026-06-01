package com.pq.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.PaymentStatus;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanOverdueTest {

    private Loan loan;
    private Borrower mockBorrower;
    private Lender mockLender;
    private List<Lender> lenders;

    @BeforeEach
    void setUp() {
        mockBorrower = mock(Borrower.class);
        when(mockBorrower.getBorrowerId()).thenReturn(new BorrowerId("B-OD-1"));
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.C);
        when(mockBorrower.getVirtualAccountBalance()).thenReturn(new Money(BigDecimal.valueOf(100_000)));

        mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new LenderId("LDR-OD-1"));

        lenders = new ArrayList<>();
        lenders.add(mockLender);

        loan = new Loan(new LoanId("L-OD-1"), new BorrowerId("B-OD-1"));
    }

    private void bringLoanToRepayment(Money amount) {
        loan.submit(mockBorrower, amount, Tenor.SIX);
        loan.startFunding();
        loan.addFunding(mockLender.getLenderId(), amount, mockLender);
        loan.disburse();
        assertEquals(LoanState.REPAYMENT, loan.getState());
    }

    @Test
    void testGracePeriod1Hari_StatusTetapUnpaid_PenaltyNol() {
        bringLoanToRepayment(new Money(BigDecimal.valueOf(12_000_000)));

        Payment cicilan = loan.getPayments().get(0);
        LocalDate satuHariSetelahJatuhTempo = cicilan.getDueDate().plusDays(1);

        cicilan.checkOverdue(satuHariSetelahJatuhTempo);

        assertEquals(PaymentStatus.UNPAID, cicilan.getStatus());
        assertEquals(BigDecimal.ZERO, cicilan.getPenalty().getAmount());
    }

    @Test
    void testLewatGracePeriod5Hari_StatusJadiOverdue() {
        bringLoanToRepayment(new Money(BigDecimal.valueOf(12_000_000)));

        Payment cicilan = loan.getPayments().get(0);
        LocalDate limaHariSetelahJatuhTempo = cicilan.getDueDate().plusDays(5);

        cicilan.checkOverdue(limaHariSetelahJatuhTempo);

        assertEquals(PaymentStatus.OVERDUE, cicilan.getStatus());
    }

    @Test
    void testDenda7HariOverdue_PadaCicilan2Juta() {
        bringLoanToRepayment(new Money(BigDecimal.valueOf(12_000_000)));

        Payment cicilan = loan.getPayments().get(0);
        // denda = 0.1% × totalAmount × jumlahHariOverdue
        // totalAmount cicilan flat Grade C, pokok 12jt/6 = 2.000.000, bunga = 12jt*0.18/12 = 180.000
        // totalAmount = 2.180.000 (tapi denda dihitung dari cicilan pokok per bulan = 2.000.000)
        // denda = 0.001 × 2.000.000 × 7 = 14.000
        LocalDate tujuhHariSetelahJatuhTempo = cicilan.getDueDate().plusDays(7);

        cicilan.checkOverdue(tujuhHariSetelahJatuhTempo);

        BigDecimal expectedPenalty = BigDecimal.valueOf(14_000);
        assertEquals(0, expectedPenalty.compareTo(cicilan.getPenalty().getAmount()),
                "Denda 7 hari overdue harus 14.000, bukan " + cicilan.getPenalty().getAmount());
    }

    @Test
    void testDendaDiCapMaksimal100PersenPokokPinjaman() {
        Money pokokPinjaman = new Money(BigDecimal.valueOf(12_000_000));
        bringLoanToRepayment(pokokPinjaman);

        Payment cicilan = loan.getPayments().get(0);
        // Simulasi overdue sangat lama sehingga denda melebihi pokok
        LocalDate sangatTerlambat = cicilan.getDueDate().plusDays(99999);

        cicilan.checkOverdue(sangatTerlambat, pokokPinjaman);

        assertTrue(cicilan.getPenalty().getAmount().compareTo(pokokPinjaman.getAmount()) <= 0,
                "Denda tidak boleh melebihi 100% pokok pinjaman awal");
    }

    @Test
    void testMakeRepayment_DitolakJikaOverdueTanpaDenda() {
        bringLoanToRepayment(new Money(BigDecimal.valueOf(12_000_000)));

        Payment cicilan = loan.getPayments().get(0);
        LocalDate terlambat = cicilan.getDueDate().plusDays(7);
        cicilan.checkOverdue(terlambat);

        // Bayar hanya cicilan tanpa denda → harus ditolak
        assertThrows(IllegalArgumentException.class, () ->
                loan.makeRepayment(cicilan.getPaymentId(), lenders, cicilan.getTotalAmount())
        );
    }

    @Test
    void testMakeRepayment_BerhasilDenganDenda_DendaTidakKeDistribusiKeLender() {
        bringLoanToRepayment(new Money(BigDecimal.valueOf(12_000_000)));

        Payment cicilan = loan.getPayments().get(0);
        LocalDate terlambat = cicilan.getDueDate().plusDays(7);
        cicilan.checkOverdue(terlambat);

        Money penalty = cicilan.getPenalty();
        Money totalBayar = new Money(cicilan.getTotalAmount().getAmount().add(penalty.getAmount()));

        BigDecimal lenderBalanceBefore = mockLender.getVirtualAccountBalance().getAmount();

        loan.makeRepayment(cicilan.getPaymentId(), lenders, totalBayar);

        assertEquals(PaymentStatus.PAID, cicilan.getStatus());

        // Verifikasi denda TIDAK didistribusikan ke lender
        // Lender hanya menerima porsi dari cicilan (tanpa denda)
        BigDecimal expectedLenderShare = cicilan.getTotalAmount().getAmount();
        verify(mockLender, never()).addBalance(new Money(totalBayar.getAmount()));
    }
}
