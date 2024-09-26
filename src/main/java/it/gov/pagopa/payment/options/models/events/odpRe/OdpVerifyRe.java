package it.gov.pagopa.payment.options.models.events.odpRe;

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
public class OdpVerifyRe {

  private Body body;
  private Properties properties;

}
