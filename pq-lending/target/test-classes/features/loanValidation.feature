Feature: Validasi Pengajuan Pinjaman

  # BR-01: Ketentuan Grade
  Scenario: Borrower grade A memiliki limit Rp 500 juta
    Given borrower dengan grade A
    Then limit maksimal pinjamannya adalah 500000000

  Scenario: Borrower grade B memiliki limit Rp 200 juta
    Given borrower dengan grade B
    Then limit maksimal pinjamannya adalah 200000000

  Scenario: Borrower grade C memiliki limit Rp 50 juta
    Given borrower dengan grade C
    Then limit maksimal pinjamannya adalah 50000000

  Scenario: Borrower grade D memiliki limit Rp 10 juta
    Given borrower dengan grade D
    Then limit maksimal pinjamannya adalah 10000000

  Scenario: Borrower grade A tenor yang diizinkan adalah 3 6 12 24 bulan
    Given borrower dengan grade A
    Then tenor yang diizinkan adalah 3 6 12 24

  Scenario: Borrower grade D tenor yang diizinkan adalah 1 dan 3 bulan
    Given borrower dengan grade D
    Then tenor yang diizinkan adalah 1 3

  # BR-02: Validasi Amount
  Scenario: Pengajuan ditolak jika amount nol
    Given borrower mengajukan pinjaman
    When borrower mengajukan pinjaman sebesar 0
    Then pengajuan ditolak dengan pesan "Amount harus lebih dari 0"

  Scenario: Pengajuan ditolak jika amount negatif
    Given borrower mengajukan pinjaman
    When borrower mengajukan pinjaman sebesar -1000000
    Then pengajuan ditolak dengan pesan "Amount harus lebih dari 0"

  Scenario: Pengajuan ditolak jika amount melebihi limit grade
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 100000000
    Then pengajuan ditolak dengan pesan "Amount melebihi limit grade"

  Scenario: Pengajuan diterima jika amount dalam batas grade
    Given borrower dengan grade C
    When borrower mengajukan pinjaman sebesar 30000000
    Then pengajuan tidak ditolak karena amount sesuai dengan limit grade

  # BR-03: Validasi Tenor
  Scenario: Pengajuan ditolak jika tenor tidak tersedia untuk grade
    Given borrower dengan grade D
    When borrower mengajukan tenor 12 bulan
    Then pengajuan ditolak dengan pesan "Tenor tidak tersedia untuk grade ini"

  Scenario: Pengajuan diterima jika tenor tersedia untuk grade
    Given borrower dengan grade D
    When borrower mengajukan tenor 3 bulan
    Then pengajuan tidak ditolak karena tenor