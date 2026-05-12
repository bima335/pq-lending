package com.pq.bdd.steps;

import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import com.pq.domain.model.loan.*;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.enums.*;
import com.pq.domain.model.valueobject.*;
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
        }
    }

    @Given("loan dengan status {string}")
    public void loanDenganStatus(String status) {
        LoanState parsed = LoanState.valueOf(status);
        LoanId loanId = new LoanId("L001");
        BorrowerId borrowerId = new BorrowerId("B001");
        this.loan = new Loan(loanId, borrowerId);
        this.loan.setState(parsed);
    }

    @Given("belum ada lender yang berkontribusi")
    public void belumAdaLenderYangBerkontribusi() {
        ensureLoanExists();
        this.loan.getFundings().clear();
        this.lenders.clear();
    }

    @Given("loan dengan target {long}")
    public void loanDenganTarget(long target) {
        ensureLoanExists();
        this.loan.setAmount(new Money(BigDecimal.valueOf(target)));
    }

    @Given("total terkumpul saat ini adalah {long}")
    public void totalTerkumpulSaatIniAdalah(long terkumpul) {
        ensureLoanExists();
        LenderId lenderId = new LenderId("L001");
        Lender lender = new Lender(lenderId, "Lender1", new Money(BigDecimal.valueOf(1000000)));
        Money amount = new Money(BigDecimal.valueOf(terkumpul));
        this.lenders.add(lender);
        this.lenderInitialBalances.put(lenderId.getValue(), lender.getVirtualAccountBalance());
        this.loan.addFunding(lenderId, amount, lender);
    }

    @Given("saldo virtual account borrower adalah {long}")
    public void saldoVirtualAccountBorrowerAdalah(long saldo) {
        BorrowerId borrowerId = new BorrowerId("B001");
        this.borrower = new Borrower(borrowerId, "Borrower1", Grade.A, new Money(BigDecimal.valueOf(saldo)));
    }

    @Given("loan dengan status DISBURSED")
    public void loanDenganStatusDISBURSED() {
        loanDenganStatus("DISBURSED");
    }

    @Given("loan dengan target {long} dan tenor {int} bulan")
    public void loanDenganTargetDanTenorBulan(long target, int tenor) {
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

    @When("lender mendanai sebesar {long}")
    public void lenderMendanaiSebesar(long amount) {
        ensureLoanExists();
        LenderId lenderId = new LenderId("L002");
        Lender lender = new Lender(lenderId, "Lender2", new Money(BigDecimal.valueOf(10000000)));
        Money fundingAmount = new Money(BigDecimal.valueOf(amount));
        this.lenders.add(lender);
        this.lenderInitialBalances.put(lenderId.getValue(), lender.getVirtualAccountBalance());
        this.loan.addFunding(lenderId, fundingAmount, lender);
    }

    @When("funding mencapai {int} persen")
    public void fundingMencapaiPersen(int persen) {
        ensureLoanExists();
        if (persen == 100) {
            this.loan.disburse();
        }
    }

    @When("borrower mencoba membatalkan pinjaman")
    public void borrowerMencobaMembatalkanPinjaman() {
        borrowerMembatalkanPinjaman();
    }

    @Then("status loan berubah menjadi {string}")
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

    @Then("status loan tetap {string}")
    public void statusLoanTetap(String state) {
        LoanState expectedState = LoanState.valueOf(state);
        Assertions.assertEquals(expectedState, this.loan.getState());
    }

    @Then("jadwal cicilan dibuat sebanyak {int}")
    public void jadwalCicilanDibuatSebanyak(int jumlah) {
        Assertions.assertEquals(jumlah, this.loan.getPayments().size());
    }
}