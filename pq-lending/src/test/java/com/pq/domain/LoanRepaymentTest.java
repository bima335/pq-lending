package com.pq.domain;

import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.PaymentStatus;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanRepaymentTest {

    private Loan loan;
    private Borrower mockBorrower;
    private List<Lender> mockLenders;

    @BeforeEach
    void setUp() {
        mockBorrower = mock(Borrower.class);
        mockLenders = new ArrayList<>();

        // Setup default loan (belum disubmit)
        loan = new Loan(new LoanId("L-123"), new BorrowerId("B-123"));
    }

    // ==========================================
    // 1. PEMBUATAN JADWAL CICILAN (DISBURSE)
    // ==========================================

    // Check
    @Test
    @DisplayName("Jadwal cicilan dibuat sekaligus saat loan DISBURSED")
    void testJadwalCicilanDibuatSaatDisbursed() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.SIX);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos validasi disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();

        // Assert
        assertEquals(LoanState.REPAYMENT, loan.getState(), "Status loan harus DISBURSED");
        assertEquals(6, loan.getPayments().size(), "Jumlah cicilan harus sesuai tenor (6 bulan)");

        for (Payment payment : loan.getPayments()) {
            assertEquals(PaymentStatus.UNPAID, payment.getStatus(), "Cicilan baru harus berstatus UNPAID");
        }
    }

    // Check
    @Test
    @DisplayName("Tanggal cicilan pertama adalah 1 bulan setelah DISBURSED")
    void testTanggalCicilanDanJatuhTempo() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Masuk ke fase funding
        loan.startFunding();

        // 2. Simulasi pendanaan 100% agar disburse() memproses jadwal cicilan
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse(); // Asumsi disburse mencatat tanggal hari ini
        LocalDate today = LocalDate.now();

        // Assert
        List<Payment> payments = loan.getPayments();
        assertEquals(today.plusMonths(1), payments.get(0).getDueDate(), "Cicilan 1 jatuh tempo 1 bulan lagi");
        assertEquals(today.plusMonths(2), payments.get(1).getDueDate(), "Cicilan 2 jatuh tempo 2 bulan lagi");
        assertEquals(today.plusMonths(3), payments.get(2).getDueDate(), "Cicilan 3 jatuh tempo 3 bulan lagi");
    }

    // ==========================================
    // 2. PERHITUNGAN BUNGA (FLAT & EFFECTIVE)
    // ==========================================

    // Check
    @Test
    @DisplayName("Cicilan Flat Rate sama setiap bulan (Grade C/D)")
    void testPerhitunganCicilanFlatRate() {
        // Arrange - Grade C (Flat Rate)
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.C);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();
        List<Payment> payments = loan.getPayments();

        // Assert
        BigDecimal cicilan1 = payments.get(0).getTotalAmount().getAmount();
        BigDecimal cicilan2 = payments.get(1).getTotalAmount().getAmount();
        BigDecimal cicilan3 = payments.get(2).getTotalAmount().getAmount();

        assertEquals(0, cicilan1.compareTo(cicilan2), "Cicilan bulan 1 dan 2 harus sama");
        assertEquals(0, cicilan2.compareTo(cicilan3), "Cicilan bulan 2 dan 3 harus sama");
    }

    // Pending
    @Test
    @DisplayName("Cicilan Effective Rate mengecil setiap bulan (Grade A/B)")
    void testPerhitunganCicilanEffectiveRate() {
        // Arrange - Grade A (Effective Rate)
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();
        List<Payment> payments = loan.getPayments();

        assertFalse(payments.isEmpty(), "Jadwal cicilan harus sudah terbentuk");

        BigDecimal cicilan1 = payments.get(0).getTotalAmount().getAmount();
        BigDecimal cicilan2 = payments.get(1).getTotalAmount().getAmount();
        BigDecimal cicilan3 = payments.get(2).getTotalAmount().getAmount();

        assertTrue(cicilan1.compareTo(cicilan2) > 0, "Cicilan bulan 1 harus lebih besar dari bulan 2");
        assertTrue(cicilan2.compareTo(cicilan3) > 0, "Cicilan bulan 2 harus lebih besar dari bulan 3");
    }

    // ==========================================
    // 3. PEMBAYARAN CICILAN (REPAYMENT)
    // ==========================================

    // check
    @Test
    @DisplayName("Pembayaran ditolak jika amount tidak sesuai")
    void testPembayaranDitolakJikaAmountTidakSesuai() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse(); // Status menjadi REPAYMENT

        Payment payment = loan.getPayments().get(0);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Money invalidAmount = new Money(new BigDecimal("500000"));
            loan.makeRepayment(payment.getPaymentId(), mockLenders, invalidAmount);
        });

        assertEquals("Jumlah pembayaran tidak sesuai", exception.getMessage());
    }

    @Test
    @DisplayName("Status cicilan menjadi PAID saat dibayar dengan benar")
    void testPembayaranCicilanBerhasil() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();

        Payment cicilanPertama = loan.getPayments().get(0);

        // Act
        // Asumsi makeRepayment menerima PaymentId dan memproses pembayaran
        loan.makeRepayment(cicilanPertama.getPaymentId(), mockLenders, cicilanPertama.getTotalAmount());

        // Assert
        assertEquals(PaymentStatus.PAID, cicilanPertama.getStatus(), "Status cicilan pertama harus PAID");
        assertNotNull(cicilanPertama.getPaidDate(), "Tanggal bayar harus tercatat");
        assertEquals(LoanState.REPAYMENT, loan.getState(), "Status loan masih REPAYMENT karena ada sisa cicilan");
    }

    // ==========================================
    // 4. PENUTUPAN LOAN (CLOSED)
    // ==========================================

    @Test
    @DisplayName("Loan otomatis CLOSED setelah SEMUA cicilan dibayar")
    void testLoanOtomatisClosed() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.A);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.THREE);
        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();

        List<Payment> semuaCicilan = loan.getPayments();
        for (Payment cicilan : semuaCicilan) {
            // Sebelum dibayar, status loan masih REPAYMENT
            assertEquals(LoanState.REPAYMENT, loan.getState());
            
            // Bayar cicilan
            loan.makeRepayment(cicilan.getPaymentId(), mockLenders, cicilan.getTotalAmount());
        }

        for (Payment cicilan : semuaCicilan) {
            assertEquals(PaymentStatus.PAID, cicilan.getStatus(), "Semua cicilan harus lunas");
        }
        
        assertEquals(LoanState.CLOSED, loan.getState(), "Loan otomatis CLOSED setelah semua cicilan lunas");
    }

    @Test
    @DisplayName("Loan berstatus CLOSED tidak bisa diubah")
    void testLoanClosedTidakBisaDiubah() {
        // Arrange
        when(mockBorrower.getCreditGrade()).thenReturn(Grade.C);
        Money targetAmount = new Money(new BigDecimal("12000000"));
        loan.submit(mockBorrower, targetAmount, Tenor.ONE);

        loan.validate();

        // 1. Pindahkan ke fase funding
        loan.startFunding();

        // 2. Beri pendanaan hingga 100% agar lolos guard clause di disburse()
        Lender mockLender = mock(Lender.class);
        when(mockLender.getLenderId()).thenReturn(new com.pq.domain.model.valueobject.LenderId("LDR-1"));
        loan.addFunding(new com.pq.domain.model.valueobject.LenderId("LDR-1"), targetAmount, mockLender);

        loan.disburse();

        Payment cicilan = loan.getPayments().get(0);
        loan.makeRepayment(cicilan.getPaymentId(), mockLenders, cicilan.getTotalAmount());

        // Assert loan is CLOSED
        assertEquals(LoanState.CLOSED, loan.getState());

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            // Mencoba merubah status atau memanipulasi loan yang sudah CLOSED
            loan.cancel(mockBorrower, mockLenders);
        });

        assertEquals("Loan tidak dapat dibatalkan setelah dana cair", exception.getMessage());
    }
}