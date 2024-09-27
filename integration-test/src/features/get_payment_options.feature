Feature: Get Payment Options

  Scenario: Unauthorized idPsp
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "single" and idPsp "invalid"
    Then response has an http status that contains 40

  Scenario: Nav not allowed for OdP
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "invalid" and idPsp "valid"
    Then response has a 400 Http status
    And has error code "ODP-015"

  Scenario: Station unavailable for provided noticeNumber
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "missing" and idPsp "valid"
    Then response has a 404 Http status
    And has error code "ODP-009"

  Scenario: Station unavailable for provided fiscalCode
    When an Http GET request is sent to recover payment options for taxCode "invalid" with noticeNumber "valid" and idPsp "valid"
    Then response has a 404 Http status
    And has error code "ODP-009"

  Scenario: Station is disabled
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "disabled" and idPsp "valid"
    Then response has a 400 Http status
    And has error code "ODP-0099"

  Scenario: Station has odp flag disabled
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "disabledOdp" and idPsp "valid"
    Then response has a 400 Http status
    And has error code "ODP-016"

  Scenario: Retrieve Payment Options (Opzione Unica)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "single" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 1
    And payments option n 0 has 1 installments

  Scenario: Retrieve Payment Options (Opzione Unica + Unico Piano Rateale)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "singleAndMultiple" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 2
    And payments option n 0 has 1 installments
    And payments option n 1 has 3 installments

  Scenario: Retrieve Payment Options (Opzione Unica + Molteplici Piani Rateali)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "singleAndManyMultiples" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 3
    And payments option n 0 has 1 installments
    And payments option n 1 has 3 installments
    And payments option n 2 has 5 installments

  Scenario: Retrieve Payment Options (Co-Obbligati)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "co-oblidged" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 2
    And payments option n 0 has 1 installments
    And payments option n 1 has 1 installments
