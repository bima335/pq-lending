package com.pq.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class repaymentDanCloseSteps {

    // ==========================================
    // GIVEN STEPS
    // ==========================================

    @Given("loan grade A dengan pokok {int} tenor {int} bulan rate {int} persen per tahun")
    public void loan_grade_a_dengan_pokok_tenor_bulan_rate_persen_per_tahun(Integer int1, Integer int2, Integer int3) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan grade C dengan pokok {int} tenor {int} bulan rate {int} persen per tahun")
    public void loan_grade_c_dengan_pokok_tenor_bulan_rate_persen_per_tahun(Integer int1, Integer int2, Integer int3) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan dengan dua lender porsi {double} dan {double}")
    public void loan_dengan_dua_lender_porsi_dan(Double double1, Double double2) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("cicilan pertama sebesar {int}")
    public void cicilan_pertama_sebesar(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan tenor {int} bulan yang baru DISBURSED")
    public void loan_tenor_bulan_yang_baru_disbursed(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan dengan status CLOSED")
    public void loan_dengan_status_closed() {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan tenor {int} bulan dengan status REPAYMENT")
    public void loan_tenor_bulan_dengan_status_repayment(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan dengan status REPAYMENT dan cicilan pertama UNPAID")
    public void loan_dengan_status_repayment_dan_cicilan_pertama_unpaid() {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan dengan cicilan pertama sebesar {int}")
    public void loan_dengan_cicilan_pertama_sebesar(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan dengan semua cicilan sudah PAID")
    public void loan_dengan_semua_cicilan_sudah_paid() {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan tenor {int} bulan yang DISBURSED pada tanggal {int} Januari {int}")
    public void loan_tenor_bulan_yang_disbursed_pada_tanggal_januari(Integer int1, Integer int2, Integer int3) {
        throw new io.cucumber.java.PendingException();
    }

    @Given("loan yang DISBURSED pada tanggal {int} Januari {int}")
    public void loan_yang_disbursed_pada_tanggal_januari(Integer int1, Integer int2) {
        throw new io.cucumber.java.PendingException();
    }


    // ==========================================
    // WHEN STEPS
    // ==========================================

    @When("jadwal cicilan dibuat dengan EffectiveRateStrategy")
    public void jadwal_cicilan_dibuat_dengan_effective_rate_strategy() {
        throw new io.cucumber.java.PendingException();
    }

    @When("jadwal cicilan dibuat dengan FlatRateStrategy")
    public void jadwal_cicilan_dibuat_dengan_flat_rate_strategy() {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower membayar cicilan pertama")
    public void borrower_membayar_cicilan_pertama() {
        throw new io.cucumber.java.PendingException();
    }

    @When("ada aksi apapun yang mencoba mengubah loan")
    public void ada_aksi_apapun_yang_mencoba_mengubah_loan() {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower membayar satu-satunya cicilan")
    public void borrower_membayar_satu_satunya_cicilan() {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower membayar cicilan pertama saja")
    public void borrower_membayar_cicilan_pertama_saja() {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower membayar cicilan pertama dengan jumlah yang tepat")
    public void borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat() {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower membayar sebesar {int}")
    public void borrower_membayar_sebesar(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @When("borrower mencoba membayar cicilan")
    public void borrower_mencoba_membayar_cicilan() {
        throw new io.cucumber.java.PendingException();
    }


    // ==========================================
    // THEN STEPS
    // ==========================================

    @Then("cicilan bulan pertama lebih besar dari cicilan bulan kedua")
    public void cicilan_bulan_pertama_lebih_besar_dari_cicilan_bulan_kedua() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("cicilan bulan kedua lebih besar dari cicilan bulan ketiga")
    public void cicilan_bulan_kedua_lebih_besar_dari_cicilan_bulan_ketiga() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("cicilan bulan pertama sama dengan cicilan bulan kedua")
    public void cicilan_bulan_pertama_sama_dengan_cicilan_bulan_kedua() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("cicilan bulan kedua sama dengan cicilan bulan ketiga")
    public void cicilan_bulan_kedua_sama_dengan_cicilan_bulan_ketiga() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("lender pertama menerima {int}")
    public void lender_pertama_menerima(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("lender kedua menerima {int}")
    public void lender_kedua_menerima(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("jumlah cicilan yang dibuat adalah {int}")
    public void jumlah_cicilan_yang_dibuat_adalah(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("semua cicilan berstatus UNPAID")
    public void semua_cicilan_berstatus_unpaid() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("bunga bulan pertama dihitung dari pokok {int}")
    public void bunga_bulan_pertama_dihitung_dari_pokok(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("bunga bulan kedua dihitung dari sisa pokok {int}")
    public void bunga_bulan_kedua_dihitung_dari_sisa_pokok(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("aksi ditolak dengan pesan {string}")
    public void aksi_ditolak_dengan_pesan(String string) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("status loan berubah menjadi CLOSED")
    public void status_loan_berubah_menjadi_closed() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("status loan tetap REPAYMENT")
    public void status_loan_tetap_repayment() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("status cicilan pertama berubah menjadi PAID")
    public void status_cicilan_pertama_berubah_menjadi_paid() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("tanggal bayar tercatat")
    public void tanggal_bayar_tercatat() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("pembayaran ditolak dengan pesan {string}")
    public void pembayaran_ditolak_dengan_pesan(String string) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("cicilan per bulan adalah {int}")
    public void cicilan_per_bulan_adalah(Integer int1) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("tanggal cicilan kedua adalah {int} Maret {int}")
    public void tanggal_cicilan_kedua_adalah_maret(Integer int1, Integer int2) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("tanggal cicilan ketiga adalah {int} April {int}")
    public void tanggal_cicilan_ketiga_adalah_april(Integer int1, Integer int2) {
        throw new io.cucumber.java.PendingException();
    }

    @Then("tanggal cicilan pertama adalah {int} Februari {int}")
    public void tanggal_cicilan_pertama_adalah_februari(Integer int1, Integer int2) {
        throw new io.cucumber.java.PendingException();
    }
}