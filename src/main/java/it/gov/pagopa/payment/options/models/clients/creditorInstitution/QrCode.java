package it.gov.pagopa.payment.options.models.clients.creditorInstitution;

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
public class QrCode {

  private String fiscalCode;
  private String noticeNumber;

}
