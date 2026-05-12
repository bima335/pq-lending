Feature: Kontribusi Lender dan Pengecekan Deadline

  # BR-07: Kontribusi Lender
  Scenario: Kontribusi ditolak jika amount kurang dari minimum
    Given loan dengan status FUNDING dan target 10000000
    When lender mencoba mendanai sebesar 50000
    Then kontribusi ditolak dengan pesan "Minimum kontribusi adalah Rp 100.000"

  Scenario: Kontribusi diterima jika amount memenuhi minimum
    Given loan dengan status FUNDING dan target 10000000
    When lender mencoba mendanai sebesar 500000
    Then kontribusi berhasil dicatat

  Scenario: Kontribusi di-cap jika melebihi sisa target
    Given loan dengan status FUNDING dan target 10000000
    And total terkumpul saat ini adalah 9500000
    When lender mencoba mendanai sebesar 2000000
    Then kontribusi yang diterima adalah 500000

  Scenario: Kontribusi ditolak dan loan cancelled jika deadline terlewat
    Given loan dengan status FUNDING dan deadline sudah terlewat
    When lender mencoba mendanai sebesar 1000000
    Then kontribusi ditolak
    And status loan berubah menjadi CANCELLED

  # BR-08: Perhitungan Porsi Lender
  Scenario: Porsi lender dihitung berdasarkan kontribusi terhadap target
    Given loan dengan target 10000000
    When lender mendanai sebesar 3000000
    Then porsi lender adalah 0.3

  Scenario: Porsi lender diakumulasi jika mendanai lebih dari sekali
    Given loan dengan target 10000000
    And lender sudah mendanai sebesar 2000000 sebelumnya
    When lender mendanai lagi sebesar 1000000
    Then total porsi lender adalah 0.3

  Scenario: Porsi digunakan sebagai dasar distribusi cicilan
    Given loan dengan dua lender masing-masing porsi 0.6 dan 0.4
    When cicilan dibayar sebesar 1000000
    Then lender pertama menerima 600000
    And lender kedua menerima 400000

  # BR-09: Pengecekan Funding Deadline
  Scenario: Kontribusi diproses jika deadline belum terlewat
    Given loan dengan status FUNDING dan deadline belum terlewat
    When lender mencoba mendanai sebesar 1000000
    Then kontribusi berhasil diproses

  Scenario: Semua lender yang sudah berkontribusi di-refund saat deadline terlewat
    Given loan dengan status FUNDING dan deadline sudah terlewat
    And ada lender yang sudah berkontribusi 2000000
    When lender lain mencoba mendanai
    Then loan berstatus CANCELLED
    And lender yang sudah berkontribusi mendapat refund 2000000