Feature: Repayment dan Penutupan Loan

  #Pembuatan Jadwal Cicilan
  Scenario: Jadwal cicilan dibuat sekaligus saat loan DISBURSED
    Given loan tenor 6 bulan yang baru DISBURSED
    Then jumlah cicilan yang dibuat adalah 6
    And semua cicilan berstatus UNPAID

  Scenario: Tanggal cicilan pertama adalah 1 bulan setelah DISBURSED
    Given loan yang DISBURSED pada tanggal 1 Januari 2025
    Then tanggal cicilan pertama adalah 1 Februari 2025

  Scenario: Tanggal cicilan berikutnya setiap 1 bulan setelahnya
    Given loan tenor 3 bulan yang DISBURSED pada tanggal 1 Januari 2025
    Then tanggal cicilan kedua adalah 1 Maret 2025
    And tanggal cicilan ketiga adalah 1 April 2025

  #Perhitungan Cicilan Flat Rate
  Scenario: Cicilan flat rate sama setiap bulan
    Given loan grade C dengan pokok 12000000 tenor 3 bulan rate 20 persen per tahun
    When jadwal cicilan dibuat dengan FlatRateStrategy
    Then cicilan bulan pertama sama dengan cicilan bulan kedua
    And cicilan bulan kedua sama dengan cicilan bulan ketiga

  Scenario: Perhitungan cicilan flat rate benar
    Given loan grade C dengan pokok 12000000 tenor 3 bulan rate 20 persen per tahun
    When jadwal cicilan dibuat dengan FlatRateStrategy
    Then cicilan per bulan adalah 4200000

  #Perhitungan Cicilan Effective Rate
  Scenario: Cicilan effective rate mengecil setiap bulan
    Given loan grade A dengan pokok 12000000 tenor 3 bulan rate 12 persen per tahun
    When jadwal cicilan dibuat dengan EffectiveRateStrategy
    Then cicilan bulan pertama lebih besar dari cicilan bulan kedua
    And cicilan bulan kedua lebih besar dari cicilan bulan ketiga

  Scenario: Komponen bunga effective rate dihitung dari sisa pokok
    Given loan grade A dengan pokok 12000000 tenor 3 bulan rate 12 persen per tahun
    When jadwal cicilan dibuat dengan EffectiveRateStrategy
    Then bunga bulan pertama dihitung dari pokok 12000000
    And bunga bulan kedua dihitung dari sisa pokok 8000000

  #Pembayaran Cicilan
  Scenario: Pembayaran cicilan berhasil mengubah status menjadi PAID
    Given loan dengan status REPAYMENT dan cicilan pertama UNPAID
    When borrower membayar cicilan pertama dengan jumlah yang tepat
    Then status cicilan pertama berubah menjadi PAID
    And tanggal bayar tercatat

  Scenario: Pembayaran ditolak jika tidak ada cicilan UNPAID
    Given loan dengan semua cicilan sudah PAID
    When borrower mencoba membayar cicilan
    Then pembayaran ditolak dengan pesan "Tidak ada cicilan yang perlu dibayar"

  Scenario: Pembayaran ditolak jika amount tidak sesuai
    Given loan dengan cicilan pertama sebesar 1000000
    When borrower membayar sebesar 500000
    Then pembayaran ditolak dengan pesan "Jumlah pembayaran tidak sesuai"

  Scenario: Distribusi ke lender dilakukan saat cicilan dibayar
    Given loan dengan dua lender porsi 0.7 dan 0.3
    And cicilan pertama sebesar 1000000
    When borrower membayar cicilan pertama
    Then lender pertama menerima 700000
    And lender kedua menerima 300000

  #Penutupan Loan
  Scenario: Loan otomatis CLOSED setelah semua cicilan PAID
    Given loan tenor 1 bulan dengan status REPAYMENT
    When borrower membayar satu-satunya cicilan
    Then status loan berubah menjadi CLOSED

  Scenario: Loan tidak CLOSED jika masih ada cicilan UNPAID
    Given loan tenor 3 bulan dengan status REPAYMENT
    When borrower membayar cicilan pertama saja
    Then status loan tetap REPAYMENT

  Scenario: Loan CLOSED tidak bisa diubah dalam kondisi apapun
    Given loan dengan status CLOSED
    When ada aksi apapun yang mencoba mengubah loan
    Then aksi ditolak dengan pesan "Loan sudah ditutup"