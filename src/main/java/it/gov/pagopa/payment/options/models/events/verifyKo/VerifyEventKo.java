package it.gov.pagopa.payment.options.models.events.verifyKo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEventKo {

  private String id;
  private DebtorPosition debtorPosition;
  private Creditor creditor;
  private Psp psp;
  private FaultBean faultBean;
  private String serviceIdentifier;

}
