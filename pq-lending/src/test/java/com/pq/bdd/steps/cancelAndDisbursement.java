package com.pq.bdd.steps;

import io.cucumber.java.en.*;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.valueobject.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;

public class cancelAndDisbursement {
    private Loan loan;
    private Borrower borrower;
    private List<Lender> lenders = new ArrayList<>();
    private Exception exception;
    private BigDecimal saldoAwalBorrower;
    private List<BigDecimal> saldoAwalLenders = new ArrayList<>();

    @Given("loan dengan status FUNDING")
    public void loanDenganStatusFunding() {
        // Buat borrower dulu jika belum
        if (borrower == null) {
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(1000000)));
        }
        LoanId loanId = new LoanId("test-loan");
        BorrowerId borrowerId = borrower.getBorrowerId();
        loan = new Loan(loanId, borrowerId);
        // Submit loan
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding(); // Set ke FUNDING
    }
    
    @Given("belum ada lender yang berkontribusi")
    public void belumAdaLenderYangBerkontribusi() {
        // Pastikan loan sudah ada tapi fundings kosong
        if (loan == null) {
            loanDenganStatusFunding();
        }
        loan.getFundings().clear();
        lenders.clear();
    }
    
    @Given("loan dengan target {long}")
    public void loanDenganTarget(long target) {
        // Create new loan dengan target ini
        if (borrower == null) {
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(0)));
        }
        LoanId loanId = new LoanId("test-loan-" + System.nanoTime());
        BorrowerId borrowerId = borrower.getBorrowerId();
        loan = new Loan(loanId, borrowerId);
        loan.submit(borrower, new Money(BigDecimal.valueOf(target)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
    }
    
    @Given("total terkumpul saat ini adalah {long}")
    public void totalTerkumpulSaatIniAdalah(long total) {
        // Ensure loan exists
        if (loan == null) {
            // Default loan dengan target 10jt jika belum ada
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            if (borrower == null) {
                borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(0)));
            }
            LoanId loanId = new LoanId("test-loan");
            loan = new Loan(loanId, borrowerId);
            loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
            loan.validate();
            loan.startFunding();
        }
        
        // Add funding untuk mencapai total ini
        LenderId lenderId = new LenderId("test-lender-" + System.nanoTime());
        Lender lender = new Lender(lenderId, "Test Lender", new Money(BigDecimal.valueOf(0)));
        lenders.add(lender);
        FundingId fundingId = new FundingId("funding-" + System.nanoTime());
        Money amount = new Money(BigDecimal.valueOf(total));
        double portion = (double) total / 10000000; // Asumsikan target 10jt
        Funding funding = new Funding(fundingId, lenderId, amount, portion);
        loan.getFundings().add(funding);
    }
    
    @Given("saldo virtual account borrower adalah {long}")
    public void saldoVirtualAccountBorrowerAdalah(long saldo) {
        // Update borrower dengan saldo ini, atau buat baru
        if (borrower == null) {
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(saldo)));
        } else {
            // Create new borrower dengan saldo ini (karena Money immutable)
            borrower = new Borrower(borrower.getBorrowerId(), borrower.getName(), borrower.getCreditGrade(), new Money(BigDecimal.valueOf(saldo)));
        }
    }
    
    @Given("loan dengan status DISBURSED")
    public void loanDenganStatusDisbursed() {
        // Create loan dengan 100% funding
        if (borrower == null) {
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(1000000)));
        }
        LoanId loanId = new LoanId("test-loan-" + System.nanoTime());
        BorrowerId borrowerId = borrower.getBorrowerId();
        loan = new Loan(loanId, borrowerId);
        loan.submit(borrower, new Money(BigDecimal.valueOf(10000000)), Tenor.THREE);
        loan.validate();
        loan.startFunding();
        
        // Add funding 100%
        LenderId lenderId = new LenderId("lender-100pct");
        Lender lender = new Lender(lenderId, "Full Lender", new Money(BigDecimal.valueOf(0)));
        lenders.clear();
        lenders.add(lender);
        FundingId fundingId = new FundingId("funding-100pct");
        Funding funding = new Funding(fundingId, lenderId, new Money(BigDecimal.valueOf(10000000)), 1.0);
        loan.getFundings().add(funding);
        
        // Auto trigger disburse
        loan.disburse();
    }
    
    @Given("loan dengan target {long} dan tenor {int} bulan")
    public void loanDenganTargetDanTenorBulan(long target, int tenor) {
        // Create loan dengan target dan tenor
        if (borrower == null) {
            BorrowerId borrowerId = new BorrowerId("test-borrower");
            borrower = new Borrower(borrowerId, "Test Borrower", Grade.A, new Money(BigDecimal.valueOf(1000000)));
        }
        LoanId loanId = new LoanId("test-loan-" + System.nanoTime());
        BorrowerId borrowerId = borrower.getBorrowerId();
        loan = new Loan(loanId, borrowerId);
        Tenor tenorEnum = Tenor.fromMonths(tenor);
        loan.submit(borrower, new Money(BigDecimal.valueOf(target)), tenorEnum);
        loan.validate();
        loan.startFunding();
    }
    
    @When("borrower membatalkan pinjaman")
    public void borrowerMembatalkanPinjaman() {
        // Simpan saldo awal
        saldoAwalBorrower = borrower.getVirtualAccountBalance().getAmount();
        for (Lender l : lenders) {
            saldoAwalLenders.add(l.getVirtualAccountBalance().getAmount());
        }
        try {
            loan.cancel(borrower, lenders);
            exception = null;
        } catch (Exception e) {
            exception = e;
        }
    }
    
    @When("borrower mencoba membatalkan pinjaman")
    public void borrowerMencobaMembatalkanPinjaman() {
        // Sama seperti di atas, karena "mencoba" berarti attempt
        borrowerMembatalkanPinjaman();
    }
    
    @When("lender mendanai sebesar {long}")
    public void lenderMendanaiSebesar(long amount) {
        // Tambahkan funding ke loan
        LenderId lenderId = new LenderId("test-lender-2");
        Lender lender = new Lender(lenderId, "Test Lender 2", new Money(BigDecimal.valueOf(10000000)));
        lenders.add(lender);
        loan.addFunding(lenderId, new Money(BigDecimal.valueOf(amount)), lender);
    }
    
    @When("funding mencapai 100 persen")
    public void fundingMencapai100Persen() {
        if (loan == null) {
            throw new IllegalStateException("Loan harus dibuat sebelum funding mencapai 100 persen");
        }
        // Tambahkan funding sebesar sisa target agar funding jadi 100%
        Money totalFunded = loan.getTotalFunded();
        BigDecimal remaining = loan.getAmount().getAmount().subtract(totalFunded.getAmount());
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            LenderId lenderId = new LenderId("test-lender-100pct");
            Lender lender = new Lender(lenderId, "Test Lender 100%", new Money(BigDecimal.valueOf(0)));
            lenders.add(lender);
            loan.addFunding(lenderId, new Money(remaining), lender);
        }
        loan.disburse();
    }
    
    @Then("status loan berubah menjadi CANCELLED")
    public void statusLoanBerubahMenjadiCancelled() {
        Assertions.assertEquals(LoanState.CANCELLED, loan.getState());
    }
    
    @Then("tidak ada denda yang dikenakan")
    public void tidakAdaDendaYangDikenakan() {
        // Assert bahwa saldo borrower tidak berkurang
        Assertions.assertEquals(saldoAwalBorrower, borrower.getVirtualAccountBalance().getAmount());
    }
    
    @Then("denda yang dipotong adalah {long}")
    public void dendaYangDipotongAdalah(long denda) {
        // Assert bahwa saldo borrower berkurang sebesar denda
        BigDecimal expected = saldoAwalBorrower.subtract(BigDecimal.valueOf(denda));
        Assertions.assertEquals(expected, borrower.getVirtualAccountBalance().getAmount());
    }
    
    @Then("semua lender mendapat refund penuh sesuai porsi")
    public void semuaLenderMendapatRefundPenuhSesuaiPorsi() {
        // Assert bahwa balance lender bertambah sesuai funding mereka
        for (int i = 0; i < lenders.size(); i++) {
            Lender l = lenders.get(i);
            BigDecimal saldoAwal = saldoAwalLenders.get(i);
            // Asumsikan refund penuh, jadi tambah amount funding
            // Misalnya, cari funding untuk lender ini
            Money funded = loan.getFundings().stream()
                .filter(f -> f.getLenderId().equals(l.getLenderId()))
                .map(Funding::getAmount)
                .findFirst().orElse(new Money(BigDecimal.ZERO));
            BigDecimal expected = saldoAwal.add(funded.getAmount());
            Assertions.assertEquals(expected, l.getVirtualAccountBalance().getAmount());
        }
    }
    
    @Then("pembatalan ditolak dengan pesan {string}")
    public void pembatalanDitolakDenganPesan(String pesan) {
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(pesan, exception.getMessage());
    }
    
    @Then("status loan berubah menjadi DISBURSED")
    public void statusLoanBerubahMenjadiDisbursed() {
        Assertions.assertEquals(LoanState.DISBURSED, loan.getState());
    }
    
    @Then("status loan tetap FUNDING")
    public void statusLoanTetapFunding() {
        Assertions.assertEquals(LoanState.FUNDING, loan.getState());
    }
    
    @Then("jadwal cicilan dibuat sebanyak {int}")
    public void jadwalCicilanDibuatSebanyak(int jumlah) {
        Assertions.assertEquals(jumlah, loan.getPayments().size());
    }
    
    @Then("status loan berubah menjadi REPAYMENT")
    public void statusLoanBerubahMenjadiRepayment() {
        Assertions.assertEquals(LoanState.REPAYMENT, loan.getState());
    }
}
