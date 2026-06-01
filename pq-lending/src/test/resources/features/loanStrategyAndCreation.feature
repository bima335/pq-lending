Feature: Pembuatan Loan dan Penentuan Strategy

  # BR-04: Penentuan Interest Strategy
  Scenario: Grade A mendapatkan EffectiveRateStrategy
    Given borrower dengan grade A
    When loan berhasil dibuat
    Then strategy bunga yang digunakan adalah EffectiveRateStrategy

  Scenario: Grade B mendapatkan EffectiveRateStrategy
    Given borrower dengan grade B
    When loan berhasil dibuat
    Then strategy bunga yang digunakan adalah EffectiveRateStrategy

  Scenario: Grade C mendapatkan FlatRateStrategy
    Given borrower dengan grade C
    When loan berhasil dibuat
    Then strategy bunga yang digunakan adalah FlatRateStrategy

  Scenario: Grade D mendapatkan FlatRateStrategy
    Given borrower dengan grade D
    When loan berhasil dibuat
    Then strategy bunga yang digunakan adalah FlatRateStrategy

  Scenario: Strategy ditentukan otomatis tidak bisa diubah borrower
    Given borrower dengan grade C
    When loan berhasil dibuat
    Then strategy tidak bisa diubah setelah loan dibuat

  # BR-05: Pembuatan Loan
  Scenario: Loan berhasil dibuat dengan status SUBMITTED lalu VALIDATED
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 30000000 dengan tenor 6 bulan
    Then loan berhasil dibuat
    And status loan adalah VALIDATED

  Scenario: Loan tidak bisa dibuat jika validasi amount gagal
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 100000000 dengan tenor 6 bulan
    Then loan tidak berhasil dibuat

  Scenario: Loan tidak bisa dibuat jika validasi tenor gagal
    Given borrower dengan grade D
    When borrower mengajukan pinjaman sebesar 5000000 dengan tenor 24 bulan
    Then loan tidak berhasil dibuat

  # BR-06: Ketentuan Masuk Fase Funding
  Scenario: Loan yang tervalidasi masuk ke fase FUNDING
    Given loan dengan status VALIDATED
    When sistem memulai fase funding
    Then status loan berubah menjadi FUNDING

  Scenario: Funding deadline ditetapkan 14 hari kerja
    Given loan dengan status VALIDATED
    When sistem memulai fase funding
    Then funding deadline adalah 14 hari kerja dari sekarang

  Scenario: Total terkumpul awal adalah nol
    Given loan baru masuk fase FUNDING
    Then total dana terkumpul adalah 0
