package com.pq.bdd.steps;

import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import com.pq.domain.model.loan.*;
import com.pq.domain.model.loan.observer.AutoDisbursementObserver;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.enums.*;
import com.pq.domain.model.valueobject.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CancelDanDisbursementSteps {
    private Loan loan;
    private Borrower borrower;
    private List<Lender> lenders = new ArrayList<>();
    private Map<String, Money> lenderInitialBalances = new HashMap<>();
    private Exception exception;

    private void ensureLoanExists() {
        if (this.loan == null) {
            LoanId loanId = new LoanId("L001");
            BorrowerId borrowerId = new BorrowerId("B001");
            this.loan = new Loan(loanId, borrowerId);
            this.loan.setState(LoanState.FUNDING);
            this.loan.determineInterestStrategy(Grade.A);
            this.loan.setTenor(Tenor.SIX);
            this.loan.addObserver(new AutoDisbursementObserver());
        }
    }

    @Given("loan berada pada status {word}")
    public void loanDenganStatus(String status) {
        LoanState parsed = LoanState.valueOf(status);
        LoanId loanId = new LoanId("L001");
        BorrowerId borrowerId = new BorrowerId("B001");
        this.loan = new Loan(loanId, borrowerId);
        this.loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        this.loan.determineInterestStrategy(Grade.A);
        this.loan.setState(parsed);
        this.loan.addObserver(new AutoDisbursementObserver());
    }

    @Given("loan pembatalan dengan target {int}")
    public void loanPembatalanDenganTarget(int target) {
        ensureLoanExists();
        this.loan.setAmount(new Money(BigDecimal.valueOf(target)));
    }

    @Given("total terkumpul pembatalan saat ini adalah {int}")
    public void totalTerkumpulPembatalanSaatIniAdalah(int terkumpul) {
        ensureLoanExists();
        LenderId lenderId = new LenderId("L001");
        Lender lender = new Lender(lenderId, "Lender1", new Money(BigDecimal.valueOf(1000000)));
        Money amount = new Money(BigDecimal.valueOf(terkumpul));
        this.lenders.add(lender);
        this.lenderInitialBalances.put(lenderId.getValue(), lender.getVirtualAccountBalance());
        this.loan.addFunding(lenderId, amount, lender);
    }

    @Given("belum ada lender yang berkontribusi")
    public void belumAdaLenderYangBerkontribusi() {
        ensureLoanExists();
        this.loan.getFundings().clear();
        this.lenders.clear();
    }

    @Given("saldo virtual account borrower adalah {long}")
    public void saldoVirtualAccountBorrowerAdalah(long saldo) {
        BorrowerId borrowerId = new BorrowerId("B001");
        this.borrower = new Borrower(borrowerId, "Borrower1", Grade.A, new Money(BigDecimal.valueOf(saldo)));
    }

    @Given("loan pembatalan dengan target {int} dan tenor {int} bulan")
    public void loanDenganTargetDanTenorBulan(int target, int tenor) {
        ensureLoanExists();
        this.loan.setAmount(new Money(BigDecimal.valueOf(target)));
        this.loan.setTenor(Tenor.fromMonths(tenor));
    }

    @When("borrower membatalkan pinjaman")
    public void borrowerMembatalkanPinjaman() {
        try {
            if (this.borrower == null) {
                saldoVirtualAccountBorrowerAdalah(1000000);
            }
            this.loan.cancel(this.borrower, this.lenders);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @When("lender mendanai loan sebesar {long}")
    public void lenderMendanaiSebesar(long amount) {
        ensureLoanExists();
        LenderId lenderId = new LenderId("L002");
        Lender lender = new Lender(lenderId, "Lender2", new Money(BigDecimal.valueOf(10000000)));
        Money fundingAmount = new Money(BigDecimal.valueOf(amount));
        this.lenders.add(lender);
        this.lenderInitialBalances.put(lenderId.getValue(), lender.getVirtualAccountBalance());
        this.loan.addFunding(lenderId, fundingAmount, lender);
        if (this.loan.getFundingPercentage() >= 100.0) {
            this.loan.disburse();
        }
    }

    @When("funding mencapai {int} persen")
    public void fundingMencapaiPersen(int persen) {
        ensureLoanExists();
        if (persen == 100) {
            Money amount = this.loan.getAmount();
            if (amount == null) {
                throw new IllegalStateException("Jumlah pinjaman harus ditetapkan sebelum funding 100 persen");
            }
            long funded = this.loan.getTotalFunded().getAmount().longValue();
            long needed = amount.getAmount().longValue() - funded;
            if (needed > 0) {
                LenderId lenderId = new LenderId("L100");
                Lender lender = new Lender(lenderId, "Lender100", new Money(BigDecimal.valueOf(needed)));
                this.lenders.add(lender);
                this.lenderInitialBalances.put(lenderId.getValue(), lender.getVirtualAccountBalance());
                this.loan.addFunding(lenderId, new Money(BigDecimal.valueOf(needed)), lender);
            }
            if (this.loan.getState() == LoanState.FUNDING || this.loan.getState() == LoanState.DISBURSED) {
                this.loan.disburse();
            }
        }
    }

    @When("borrower mencoba membatalkan pinjaman")
    public void borrowerMencobaMembatalkanPinjaman() {
        borrowerMembatalkanPinjaman();
    }

    @Then("status loan sekarang adalah {word}")
    public void statusLoanBerubahMenjadi(String expected) {
        LoanState expectedState = LoanState.valueOf(expected);
        Assertions.assertEquals(expectedState, this.loan.getState());
    }

    @Then("tidak ada denda yang dikenakan")
    public void tidakAdaDendaYangDikenakan() {
        Assertions.assertNotNull(this.borrower);
        Assertions.assertEquals(new BigDecimal("1000000"), this.borrower.getVirtualAccountBalance().getAmount());
    }

    @Then("denda yang dipotong adalah {long}")
    public void dendaYangDipotongAdalah(long denda) {
        Assertions.assertNotNull(this.borrower);
        Assertions.assertEquals(new BigDecimal(1000000 - denda), this.borrower.getVirtualAccountBalance().getAmount());
    }

    @Then("semua lender mendapat refund penuh sesuai porsi")
    public void semuaLenderMendapatRefundPenuhSesuaiPorsi() {
        for (Lender lender : this.lenders) {
            Money initial = this.lenderInitialBalances.get(lender.getLenderId().getValue());
            Assertions.assertNotNull(initial);
            Assertions.assertTrue(lender.getVirtualAccountBalance().getAmount().compareTo(initial.getAmount()) > 0);
        }
    }

    @Then("pembatalan ditolak dengan pesan {string}")
    public void pembatalanDitolakDenganPesan(String pesan) {
        Assertions.assertNotNull(this.exception);
        Assertions.assertEquals(pesan, this.exception.getMessage());
    }

    @Then("status loan tetap FUNDING")
    public void status_loan_tetap_funding() {
        assertEquals("FUNDING", loan.getState().toString());
    }

    @Then("jadwal cicilan dibuat sebanyak {int}")
    public void jadwalCicilanDibuatSebanyak(int jumlah) {
        Assertions.assertEquals(jumlah, this.loan.getPayments().size());
    }

    @Given("Loan berapa pada FundingState dengan {int} persen terfunding")
    public void loan_berapa_pada_funding_state_dengan_persen_terfunding(Integer persen) {
    }

    @Then("loan berpindah ke CancelledState")
    public void loan_berpindah_ke_cancelled_state() {
    }

    @Then("refund diberikan kepada semua lender sesuai porsi kontribusi")
    public void refund_diberikan_kepada_semua_lender_sesuai_porsi_kontribusi() {
    }

    @Given("loan berada pada DisbursedState")
    public void loan_berada_pada_disbursed_state() {
        LoanId loanId = new LoanId("L001");
        BorrowerId borrowerId = new BorrowerId("B001");
        this.loan = new Loan(loanId, borrowerId);
        this.loan.setAmount(new Money(BigDecimal.valueOf(10000000)));
        this.loan.determineInterestStrategy(Grade.A);
        this.loan.setState(LoanState.DISBURSED);
    }

    @Given("loan berada pada FundingState")
    public void loan_berada_pada_funding_state() {
    }

    @When("total kontribusi mencapai {int} persen dari target")
    public void total_kontribusi_mencapai_persen_dari_target(Integer persen) {
    }

    @Then("loan berpindah ke DisbursedState")
    public void loan_berpindah_ke_disbursed_state() {
    }

    @Then("jadwal cicilan dibuat otomatis")
    public void jadwal_cicilan_dibuat_otomatis() {
    }

    @Then("loan berpindah ke RepaymentState")
    public void loan_berpindah_ke_repayment_state() {
    }
}