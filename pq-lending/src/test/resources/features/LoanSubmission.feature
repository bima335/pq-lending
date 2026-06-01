Feature: Validasi Pengajuan Pinjaman

  # BR-01: Ketentuan Grade
  Scenario: Borrower grade A memiliki limit Rp 500 juta
    Given borrower dengan grade A
    Then limit maksimal pinjamannya adalah 500000000
    And tenor yang diizinkan adalah "6 12 18 24 36" bulan

  Scenario: Borrower grade B memiliki limit Rp 200 juta
    Given borrower dengan grade B
    Then limit maksimal pinjamannya adalah 200000000
    And tenor yang diizinkan adalah "6 12 18 24" bulan

  Scenario: Borrower grade C memiliki limit Rp 50 juta
    Given borrower dengan grade C
    Then limit maksimal pinjamannya adalah 50000000
    And tenor yang diizinkan adalah "6 12 18" bulan

  Scenario: Borrower grade D memiliki limit Rp 10 juta
    Given borrower dengan grade D
    Then limit maksimal pinjamannya adalah 10000000
    And tenor yang diizinkan adalah "6 12" bulan

  # BR-02: Validasi Amount
  Scenario: Pengajuan ditolak jika amount nol
    Given borrower mengajukan pinjaman
    When borrower mengajukan pinjaman sebesar 0
    Then pengajuan ditolak dengan pesan "Amount tidak valid"

  Scenario: Pengajuan ditolak jika amount negatif
    Given borrower mengajukan pinjaman
    When borrower mengajukan pinjaman sebesar -1000000
    Then pengajuan ditolak dengan pesan "Amount tidak valid"

  Scenario: Pengajuan ditolak jika amount kurang dari batas minimal
    Given borrower mengajukan pinjaman
    When borrower mengajukan pinjaman sebesar 500000
    Then pengajuan ditolak dengan pesan "Amount kurang dari batas minimal"

  Scenario: Pengajuan ditolak jika amount melebihi limit grade
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 100000000
    Then pengajuan ditolak dengan pesan "Amount melebihi limit grade"

  Scenario: Pengajuan diterima jika amount dalam batas grade dan memenuhi batas minimal
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 30000000
    Then pengajuan tidak ditolak karena amount sesuai dengan limit grade

  # BR-03: Validasi Tenor
  Scenario: Pengajuan ditolak jika tenor tidak tersedia untuk grade
    Given borrower dengan grade D
    When borrower mengajukan tenor 36 bulan
    Then pengajuan ditolak dengan pesan "Tenor tidak tersedia untuk grade ini"

  Scenario: Pengajuan diterima jika tenor tersedia untuk grade
    Given borrower dengan grade D
    When borrower mengajukan tenor 6 bulan
    Then pengajuan tidak ditolak karena tenor