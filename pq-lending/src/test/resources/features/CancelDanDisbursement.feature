Feature: Pembatalan Loan dan Pencairan Dana

# BR-10: Cancel oleh Borrower
  Scenario: Cancel tanpa kontribusi tidak ada denda
    Given loan berada pada status FUNDING
    And belum ada lender yang berkontribusi
    When borrower membatalkan pinjaman
    Then status loan sekarang adalah CANCELLED
    And tidak ada denda yang dikenakan

  Scenario: Cancel dengan kontribusi 1 sampai 50 persen kena denda 1 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 4000000
    And saldo virtual account borrower adalah 1000000
    When borrower membatalkan pinjaman
    Then denda yang dipotong adalah 40000
    And semua lender mendapat refund penuh sesuai porsi
    And status loan sekarang adalah CANCELLED

  Scenario: Cancel dengan kontribusi 51 sampai 99 persen kena denda 2 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 7000000
    And saldo virtual account borrower adalah 1000000
    When borrower membatalkan pinjaman
    Then denda yang dipotong adalah 140000
    And semua lender mendapat refund penuh sesuai porsi
    And status loan sekarang adalah CANCELLED

  Scenario: Cancel dengan kontribusi 50 persen kena denda 1 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 5000000
    And saldo virtual account borrower adalah 1000000
    When borrower membatalkan pinjaman
    Then denda yang dipotong adalah 50000
    And semua lender mendapat refund penuh sesuai porsi
    And status loan sekarang adalah CANCELLED

  Scenario: Cancel dengan kontribusi 51 persen kena denda 2 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 5100000
    And saldo virtual account borrower adalah 1000000
    When borrower membatalkan pinjaman
    Then denda yang dipotong adalah 102000
    And semua lender mendapat refund penuh sesuai porsi
    And status loan sekarang adalah CANCELLED

  Scenario: Cancel ditolak jika saldo borrower tidak cukup untuk denda
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 7000000
    And saldo virtual account borrower adalah 0
    When borrower membatalkan pinjaman
    Then pembatalan ditolak dengan pesan "Saldo tidak cukup untuk membayar denda"

  Scenario: Cancel tidak bisa dilakukan setelah loan DISBURSED
    Given loan berada pada status DISBURSED
    When borrower mencoba membatalkan pinjaman
    Then pembatalan ditolak dengan pesan "Loan tidak dapat dibatalkan setelah dana cair"

# BR-11: Trigger Disbursement
  Scenario: Loan otomatis DISBURSED saat funding mencapai 100 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 9000000
    When lender mendanai loan sebesar 1000000
    Then status loan sekarang adalah REPAYMENT

  Scenario: Loan tidak DISBURSED jika funding belum 100 persen
    Given loan pembatalan dengan target 10000000
    And total terkumpul pembatalan saat ini adalah 5000000
    When lender mendanai loan sebesar 1000000
    Then status loan tetap FUNDING

  Scenario: Jadwal cicilan dibuat setelah loan DISBURSED
    Given loan pembatalan dengan target 10000000 dan tenor 6 bulan
    When funding mencapai 100 persen
    Then jadwal cicilan dibuat sebanyak 6
    And status loan sekarang adalah REPAYMENT

# BR-10: state pattern - Cancel loan
  Scenario: Cancel dari FundingState berhasil
    Given Loan berapa pada FundingState dengan 40 persen terfunding
    When borrower membatalkan pinjaman
    Then loan berpindah ke CancelledState
    And refund diberikan kepada semua lender sesuai porsi kontribusi

    Scenario: Cancel dari DisbursedState ditolak
    Given loan berada pada DisbursedState
    When borrower mencoba membatalkan pinjaman
    Then pembatalan ditolak dengan pesan "Loan tidak dapat dibatalkan setelah dana cair"

# BR-11: state pattern - Disbursement Trigger
  Scenario: FundingState otomatis pindah ke DisbursedState saat 100 persen
    Given loan berada pada FundingState
    When total kontribusi mencapai 100 persen dari target
    Then loan berpindah ke DisbursedState
    And jadwal cicilan dibuat otomatis
    And loan berpindah ke RepaymentState