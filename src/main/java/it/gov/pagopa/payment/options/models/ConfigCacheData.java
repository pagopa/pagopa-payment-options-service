package it.gov.pagopa.payment.options.models;

import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
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
public class ConfigCacheData {

  private String cacheVersion;
  private String version;
  private ConfigDataV1 configDataV1;

}
