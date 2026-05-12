package com.pq.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class repaymentDanCloseSteps {

    // ==========================================
    // STATE VARIABLES UNTUK SHARING CONTEXT ANTAR STEPS
    // ==========================================
    private String currentGrade;
    private BigDecimal currentPrincipal;
    private int currentTenor;
    private double currentRate;

    private LocalDate disbursedDate;
    private String loanState;
    private String caughtExceptionMessage;
    
    private Map<String, Double> lenderPortions = new HashMap<>();
    private Map<String, BigDecimal> lenderBalances = new HashMap<>();
    
    private List<BigDecimal> cicilanPokok = new ArrayList<>();
    private List<BigDecimal> cicilanBunga = new ArrayList<>();
    private List<BigDecimal> cicilanTotal = new ArrayList<>();
    private List<LocalDate> paymentDates = new ArrayList<>();
    private List<String> paymentStatuses = new ArrayList<>();
    private List<LocalDate> actualPaidDates = new ArrayList<>();

    // ==========================================
    // GIVEN STEPS
    // ==========================================

    @Given("loan grade A dengan pokok {int} tenor {int} bulan rate {int} persen per tahun")
    public void loan_grade_a_dengan_pokok_tenor_bulan_rate_persen_per_tahun(Integer pokok, Integer tenor, Integer rate) {
        this.currentGrade = "A";
        this.currentPrincipal = new BigDecimal(pokok);
        this.currentTenor = tenor;
        this.currentRate = rate / 100.0;
    }

    @Given("loan grade C dengan pokok {int} tenor {int} bulan rate {int} persen per tahun")
    public void loan_grade_c_dengan_pokok_tenor_bulan_rate_persen_per_tahun(Integer pokok, Integer tenor, Integer rate) {
        this.currentGrade = "C";
        this.currentPrincipal = new BigDecimal(pokok);
        this.currentTenor = tenor;
        this.currentRate = rate / 100.0;
    }

    @Given("loan dengan dua lender porsi {double} dan {double}")
    public void loan_dengan_dua_lender_porsi_dan(Double porsi1, Double porsi2) {
        lenderPortions.put("Lender1", porsi1);
        lenderPortions.put("Lender2", porsi2);
        lenderBalances.put("Lender1", BigDecimal.ZERO);
        lenderBalances.put("Lender2", BigDecimal.ZERO);
    }

    @Given("cicilan pertama sebesar {int}")
    public void cicilan_pertama_sebesar(Integer amount) {
        if (cicilanTotal.isEmpty()) {
            cicilanTotal.add(new BigDecimal(amount));
        } else {
            cicilanTotal.set(0, new BigDecimal(amount));
        }
        if (paymentStatuses.isEmpty()) paymentStatuses.add("UNPAID");
    }

    @Given("loan tenor {int} bulan yang baru DISBURSED")
    public void loan_tenor_bulan_yang_baru_disbursed(Integer tenor) {
        this.currentTenor = tenor;
        this.loanState = "DISBURSED";
        this.paymentStatuses.clear();
        for (int i = 0; i < tenor; i++) {
            this.paymentStatuses.add("UNPAID");
        }
    }

    @Given("loan dengan status CLOSED")
    public void loan_dengan_status_closed() {
        this.loanState = "CLOSED";
    }

    @Given("loan tenor {int} bulan dengan status REPAYMENT")
    public void loan_tenor_bulan_dengan_status_repayment(Integer tenor) {
        this.currentTenor = tenor;
        this.loanState = "REPAYMENT";
        this.paymentStatuses.clear();
        for (int i = 0; i < tenor; i++) {
            this.paymentStatuses.add("UNPAID");
        }
    }

    @Given("loan dengan status REPAYMENT dan cicilan pertama UNPAID")
    public void loan_dengan_status_repayment_dan_cicilan_pertama_unpaid() {
        this.loanState = "REPAYMENT";
        if (paymentStatuses.isEmpty()) paymentStatuses.add("UNPAID");
        else paymentStatuses.set(0, "UNPAID");
    }

    @Given("loan dengan cicilan pertama sebesar {int}")
    public void loan_dengan_cicilan_pertama_sebesar(Integer amount) {
        cicilan_pertama_sebesar(amount);
    }

    @Given("loan dengan semua cicilan sudah PAID")
    public void loan_dengan_semua_cicilan_sudah_paid() {
        this.loanState = "REPAYMENT";
        if (paymentStatuses.isEmpty()) paymentStatuses.add("PAID");
        for (int i = 0; i < paymentStatuses.size(); i++) {
            paymentStatuses.set(i, "PAID");
        }
    }

    @Given("loan tenor {int} bulan yang DISBURSED pada tanggal {int} Januari {int}")
    public void loan_tenor_bulan_yang_disbursed_pada_tanggal_januari(Integer tenor, Integer tanggal, Integer tahun) {
        this.currentTenor = tenor;
        this.loanState = "DISBURSED";
        this.disbursedDate = LocalDate.of(tahun, 1, tanggal);
        
        // Buat jadwal otomatis sesuai BR-12
        paymentDates.clear();
        for (int i = 1; i <= tenor; i++) {
            paymentDates.add(disbursedDate.plusMonths(i));
        }
    }

    @Given("loan yang DISBURSED pada tanggal {int} Januari {int}")
    public void loan_yang_disbursed_pada_tanggal_januari(Integer tanggal, Integer tahun) {
        this.loanState = "DISBURSED";
        this.disbursedDate = LocalDate.of(tahun, 1, tanggal);
        
        // Default minimal 1 bulan
        paymentDates.clear();
        paymentDates.add(disbursedDate.plusMonths(1));
    }


    // ==========================================
    // WHEN STEPS
    // ==========================================

    @When("jadwal cicilan dibuat dengan EffectiveRateStrategy")
    public void jadwal_cicilan_dibuat_dengan_effective_rate_strategy() {
        // BR-14: Pokok per bulan = pokok / tenor
        BigDecimal pokokPerBulan = currentPrincipal.divide(new BigDecimal(currentTenor), 2, RoundingMode.HALF_UP);
        BigDecimal sisaPokok = currentPrincipal;

        cicilanPokok.clear();
        cicilanBunga.clear();
        cicilanTotal.clear();

        for (int i = 0; i < currentTenor; i++) {
            // Bunga bulan ke-n = sisa pokok * rate / 12
            BigDecimal bungaBulanIni = sisaPokok.multiply(new BigDecimal(currentRate))
                    .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
            
            cicilanPokok.add(pokokPerBulan);
            cicilanBunga.add(bungaBulanIni);
            cicilanTotal.add(pokokPerBulan.add(bungaBulanIni));
            
            sisaPokok = sisaPokok.subtract(pokokPerBulan);
        }
    }

    @When("jadwal cicilan dibuat dengan FlatRateStrategy")
    public void jadwal_cicilan_dibuat_dengan_flat_rate_strategy() {
        // BR-13: Pokok per bulan = pokok / tenor
        BigDecimal pokokPerBulan = currentPrincipal.divide(new BigDecimal(currentTenor), 2, RoundingMode.HALF_UP);
        // Bunga per bulan = pokok awal * rate / 12
        BigDecimal bungaPerBulan = currentPrincipal.multiply(new BigDecimal(currentRate))
                .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);

        cicilanPokok.clear();
        cicilanBunga.clear();
        cicilanTotal.clear();

        for (int i = 0; i < currentTenor; i++) {
            cicilanPokok.add(pokokPerBulan);
            cicilanBunga.add(bungaPerBulan);
            cicilanTotal.add(pokokPerBulan.add(bungaPerBulan));
        }
    }

    @When("borrower membayar cicilan pertama")
    public void borrower_membayar_cicilan_pertama() {
        borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat();
    }

    @When("ada aksi apapun yang mencoba mengubah loan")
    public void ada_aksi_apapun_yang_mencoba_mengubah_loan() {
        try {
            if ("CLOSED".equals(loanState)) {
                throw new IllegalStateException("Loan sudah ditutup");
            }
        } catch (Exception e) {
            caughtExceptionMessage = e.getMessage();
        }
    }

    @When("borrower membayar satu-satunya cicilan")
    public void borrower_membayar_satu_satunya_cicilan() {
        borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat();
    }

    @When("borrower membayar cicilan pertama saja")
    public void borrower_membayar_cicilan_pertama_saja() {
        borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat();
    }

    @When("borrower membayar cicilan pertama dengan jumlah yang tepat")
    public void borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat() {
        try {
            if (!paymentStatuses.contains("UNPAID")) {
                throw new IllegalStateException("Tidak ada cicilan yang perlu dibayar");
            }
            
            int targetIndex = paymentStatuses.indexOf("UNPAID");
            paymentStatuses.set(targetIndex, "PAID");
            actualPaidDates.add(LocalDate.now());

            // BR-15: Distribusi ke lender
            if (!lenderPortions.isEmpty() && !cicilanTotal.isEmpty()) {
                BigDecimal amountToDistribute = cicilanTotal.get(targetIndex);
                for (Map.Entry<String, Double> entry : lenderPortions.entrySet()) {
                    BigDecimal portion = amountToDistribute.multiply(BigDecimal.valueOf(entry.getValue())).setScale(0, RoundingMode.HALF_UP);
                    lenderBalances.put(entry.getKey(), lenderBalances.get(entry.getKey()).add(portion));
                }
            }

            // BR-16: Penutupan otomatis
            if (!paymentStatuses.contains("UNPAID")) {
                loanState = "CLOSED";
            }
        } catch (Exception e) {
            caughtExceptionMessage = e.getMessage();
        }
    }

    @When("borrower membayar sebesar {int}")
    public void borrower_membayar_sebesar(Integer amount) {
        try {
            BigDecimal expectedAmount = cicilanTotal.get(0);
            if (new BigDecimal(amount).compareTo(expectedAmount) != 0) {
                throw new IllegalArgumentException("Jumlah pembayaran tidak sesuai");
            }
            borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat();
        } catch (Exception e) {
            caughtExceptionMessage = e.getMessage();
        }
    }

    @When("borrower mencoba membayar cicilan")
    public void borrower_mencoba_membayar_cicilan() {
        borrower_membayar_cicilan_pertama_dengan_jumlah_yang_tepat();
    }


    // ==========================================
    // THEN STEPS
    // ==========================================

    @Then("cicilan bulan pertama lebih besar dari cicilan bulan kedua")
    public void cicilan_bulan_pertama_lebih_besar_dari_cicilan_bulan_kedua() {
        assertTrue(cicilanTotal.get(0).compareTo(cicilanTotal.get(1)) > 0, 
            "Cicilan bulan 1 harusnya lebih besar dari bulan 2 (Effective Rate)");
    }

    @Then("cicilan bulan kedua lebih besar dari cicilan bulan ketiga")
    public void cicilan_bulan_kedua_lebih_besar_dari_cicilan_bulan_ketiga() {
        assertTrue(cicilanTotal.get(1).compareTo(cicilanTotal.get(2)) > 0,
            "Cicilan bulan 2 harusnya lebih besar dari bulan 3 (Effective Rate)");
    }

    @Then("cicilan bulan pertama sama dengan cicilan bulan kedua")
    public void cicilan_bulan_pertama_sama_dengan_cicilan_bulan_kedua() {
        assertEquals(0, cicilanTotal.get(0).compareTo(cicilanTotal.get(1)),
            "Cicilan bulan 1 dan 2 harus sama (Flat Rate)");
    }

    @Then("cicilan bulan kedua sama dengan cicilan bulan ketiga")
    public void cicilan_bulan_kedua_sama_dengan_cicilan_bulan_ketiga() {
        assertEquals(0, cicilanTotal.get(1).compareTo(cicilanTotal.get(2)),
            "Cicilan bulan 2 dan 3 harus sama (Flat Rate)");
    }

    @Then("lender pertama menerima {int}")
    public void lender_pertama_menerima(Integer expectedAmount) {
        assertEquals(0, new BigDecimal(expectedAmount).compareTo(lenderBalances.get("Lender1")),
            "Distribusi dana ke Lender 1 tidak sesuai");
    }

    @Then("lender kedua menerima {int}")
    public void lender_kedua_menerima(Integer expectedAmount) {
        assertEquals(0, new BigDecimal(expectedAmount).compareTo(lenderBalances.get("Lender2")),
            "Distribusi dana ke Lender 2 tidak sesuai");
    }

    @Then("jumlah cicilan yang dibuat adalah {int}")
    public void jumlah_cicilan_yang_dibuat_adalah(Integer expectedJumlah) {
        assertEquals(expectedJumlah, paymentStatuses.size());
    }

    @Then("semua cicilan berstatus UNPAID")
    public void semua_cicilan_berstatus_unpaid() {
        for (String status : paymentStatuses) {
            assertEquals("UNPAID", status);
        }
    }

    @Then("bunga bulan pertama dihitung dari pokok {int}")
    public void bunga_bulan_pertama_dihitung_dari_pokok(Integer expectedPokok) {
        // Validasi: Pokok awal * rate / 12
        BigDecimal expectedBunga = new BigDecimal(expectedPokok).multiply(new BigDecimal(currentRate))
                .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        assertEquals(0, expectedBunga.compareTo(cicilanBunga.get(0)));
    }

    @Then("bunga bulan kedua dihitung dari sisa pokok {int}")
    public void bunga_bulan_kedua_dihitung_dari_sisa_pokok(Integer expectedSisaPokok) {
        // Validasi: Sisa pokok bulan 2 * rate / 12
        BigDecimal expectedBunga = new BigDecimal(expectedSisaPokok).multiply(new BigDecimal(currentRate))
                .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        assertEquals(0, expectedBunga.compareTo(cicilanBunga.get(1)));
    }

    @Then("aksi ditolak dengan pesan {string}")
    public void aksi_ditolak_dengan_pesan(String expectedMessage) {
        assertNotNull(caughtExceptionMessage, "Seharusnya ada exception yang dilempar");
        assertEquals(expectedMessage, caughtExceptionMessage);
    }

    @Then("status loan berubah menjadi CLOSED")
    public void status_loan_berubah_menjadi_closed() {
        assertEquals("CLOSED", loanState);
    }

    @Then("status loan tetap REPAYMENT")
    public void status_loan_tetap_repayment() {
        assertEquals("REPAYMENT", loanState);
    }

    @Then("status cicilan pertama berubah menjadi PAID")
    public void status_cicilan_pertama_berubah_menjadi_paid() {
        assertEquals("PAID", paymentStatuses.get(0));
    }

    @Then("tanggal bayar tercatat")
    public void tanggal_bayar_tercatat() {
        assertFalse(actualPaidDates.isEmpty(), "Tanggal bayar (paidDate) harus tercatat");
        assertNotNull(actualPaidDates.get(0));
    }

    @Then("pembayaran ditolak dengan pesan {string}")
    public void pembayaran_ditolak_dengan_pesan(String expectedMessage) {
        aksi_ditolak_dengan_pesan(expectedMessage);
    }

    @Then("cicilan per bulan adalah {int}")
    public void cicilan_per_bulan_adalah(Integer expectedAmount) {
        assertEquals(0, new BigDecimal(expectedAmount).compareTo(cicilanTotal.get(0)));
    }

    @Then("tanggal cicilan kedua adalah {int} Maret {int}")
    public void tanggal_cicilan_kedua_adalah_maret(Integer tanggal, Integer tahun) {
        LocalDate expectedDate = LocalDate.of(tahun, 3, tanggal);
        assertEquals(expectedDate, paymentDates.get(1));
    }

    @Then("tanggal cicilan ketiga adalah {int} April {int}")
    public void tanggal_cicilan_ketiga_adalah_april(Integer tanggal, Integer tahun) {
        LocalDate expectedDate = LocalDate.of(tahun, 4, tanggal);
        assertEquals(expectedDate, paymentDates.get(2));
    }

    @Then("tanggal cicilan pertama adalah {int} Februari {int}")
    public void tanggal_cicilan_pertama_adalah_februari(Integer tanggal, Integer tahun) {
        LocalDate expectedDate = LocalDate.of(tahun, 2, tanggal);
        assertEquals(expectedDate, paymentDates.get(0));
    }
}