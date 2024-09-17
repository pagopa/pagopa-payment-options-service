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
public class DebtorPosition {

  private String modelType;
  private String noticeNumber;
  private Long amount;

}
