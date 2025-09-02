package it.gov.pagopa.payment.options.models.clients.creditorInstitution;

import it.gov.pagopa.payment.options.models.enums.EnumInstallment;
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
public class Installment {

  private String nav;
  private String iuv;
  private Long amount;
  private String description;
  private String dueDate;
  private String validFrom;
  private EnumInstallment status;
  private String statusReason;

}
