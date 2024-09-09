package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.clients.model.ConfigDataV1;
import it.gov.pagopa.payment.options.models.CacheUpdateEvent;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ConfigCacheService {

  @Inject
  public ApiConfigCacheClient apiConfigCacheClient;

  private ConfigCacheData configCacheData;

  public ConfigCacheData getConfigCacheData() {
    return this.configCacheData;
  }

  public void checkAndUpdateCache(CacheUpdateEvent cacheUpdateEvent) {

    if (configCacheData == null ||
          !cacheUpdateEvent.getCacheVersion().equals(configCacheData.getCacheVersion()) ||
          cacheUpdateEvent.getVersion().compareTo(configCacheData.getVersion()) > 0)
    {

      if (configCacheData == null) {
        ConfigCacheData.builder().cacheVersion(
            cacheUpdateEvent.getCacheVersion()
        )
        .version(cacheUpdateEvent.getVersion())
        .build();
      }

      ConfigDataV1 configDataV1 = apiConfigCacheClient.getCache(
          List.of(new String[]{"stations", "creditorInstitutions"})
      );

      if (configDataV1.getVersion().compareTo(cacheUpdateEvent.getVersion()) >= 0) {
        configCacheData.setConfigDataV1(configDataV1);
      }

    }

  }

}
