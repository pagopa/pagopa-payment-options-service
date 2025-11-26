@setup-gpd-required
Feature: GPD Integration

  Scenario: Retrieve Payment Options (Opzione Unica)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "348111111111111111" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 1
    And payments option n 1 has 1 installments with noticeNumber "348111111111111111"

  Scenario: Retrieve Payment Options (Opzione Unica + Unico Piano Rateale)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "348111111111111121" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 2
    And payments option n 1 has 1 installments with noticeNumber "348111111111111121"
    And payments option n 2 has 3 installments with noticeNumber "348111111111111122"

  Scenario: Retrieve Payment Options (Opzione Unica + Molteplici Piani Rateali)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "348111111111111131" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 3
    And payments option n 1 has 1 installments with noticeNumber "348111111111111131"
    And payments option n 2 has 3 installments with noticeNumber "348111111111111132"
    And payments option n 3 has 5 installments with noticeNumber "348111111111111135"

  Scenario: Retrieve Payment Options (Co-Obbligati)
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "348111111111111141" and idPsp "valid"
    Then response has a 200 Http status
    And payments options has size 2
    And payments option n 1 has 1 installments with noticeNumber "348111111111111141"
    And payments option n 2 has 1 installments with noticeNumber "348111111111111142"

  Scenario: PAA Error PAA_PAGAMENTO_SCONOSCIUTO
    When an Http GET request is sent to recover payment options for taxCode "valid" with noticeNumber "348111111112222226" and idPsp "valid"
    Then response has a 502 Http status
    And has error code "ODP-023"
    And error message start with "ODP_ERRORE_EMESSO_DA_PAA ODP-107 PAA_PAGAMENTO_SCONOSCIUTO"