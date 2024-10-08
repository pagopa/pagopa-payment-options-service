package it.gov.pagopa.payment.options.models.events;

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
public class CacheUpdateEvent {

  private String cacheVersion;
  private String version;
  private String timestamp;

}
