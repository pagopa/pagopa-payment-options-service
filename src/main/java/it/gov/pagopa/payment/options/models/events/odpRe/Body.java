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
public class Body {

  private String id;
  private String insertedTimestamp;
  private EventType eventType;
  private String organizationId;
  private String stationId;
  private String pspId;
  private String brokerId;
  private String iuv;
  private String noticeNumber;
  private Status status;
  private String errorStatusDesc;
  private String errorStatusCode;
  private String sessionId;
  private String eventTimestamp;
  private String payload;
  private String version;

}
