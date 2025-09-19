package it.gov.pagopa.payment.options.models.clients.creditorInstitution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOptionsResponse {

  private String organizationFiscalCode;
  private String companyName;
  private String officeName;
  private Boolean standin;
  private List<PaymentOption> paymentOptions;

}
