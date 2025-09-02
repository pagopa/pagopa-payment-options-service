package it.gov.pagopa.payment.options.models.clients.creditorInstitution;

import it.gov.pagopa.payment.options.models.enums.EnumPo;
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
public class PaymentOption {

  private String description;
  private Integer numberOfInstallments;
  private String dueDate;
  private String validFrom;
  private Long amount;
  private EnumPo status;
  private String statusReason;
  private Boolean allCCP;
  private List<Installment> installments;

}
