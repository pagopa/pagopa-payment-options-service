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
public class Properties {

  private String primitive;
  private String sessionId;
  private String idCanale;
  private String diagnosticId;
  private String partialLog;
  private String actorClassId;
  private String serviceIdentifier;

}