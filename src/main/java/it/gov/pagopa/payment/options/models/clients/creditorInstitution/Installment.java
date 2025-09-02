package it.gov.pagopa.payment.options.models.clients.creditorInstitution;

import it.gov.pagopa.payment.options.models.enums.EnumInstallment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
  private LocalDateTime dueDate;
  private LocalDateTime validFrom;
  private EnumInstallment status;
  private String statusReason;

}
