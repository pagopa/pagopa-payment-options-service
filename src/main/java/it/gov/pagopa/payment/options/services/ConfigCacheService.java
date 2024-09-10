package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.clients.model.ConfigDataV1;
import it.gov.pagopa.payment.options.models.CacheUpdateEvent;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;

@ApplicationScoped
public class ConfigCacheService {

  @Inject
  @RestClient
  public ApiConfigCacheClient apiConfigCacheClient;

  private ConfigCacheData configCacheData;

  public ConfigCacheData getConfigCacheData() {
    return this.configCacheData != null ?
        this.configCacheData : checkAndUpdateCache(null);
  }

  public ConfigCacheData checkAndUpdateCache(CacheUpdateEvent cacheUpdateEvent) {

    if (configCacheData == null || cacheUpdateEvent == null ||
          !cacheUpdateEvent.getCacheVersion().equals(configCacheData.getCacheVersion()) ||
          cacheUpdateEvent.getVersion().compareTo(configCacheData.getVersion()) > 0)
    {

      if (configCacheData == null) {
        configCacheData = ConfigCacheData.builder().cacheVersion(
            cacheUpdateEvent != null ?
                cacheUpdateEvent.getCacheVersion() :
                null
        )
        .version(cacheUpdateEvent != null ?
            cacheUpdateEvent.getVersion() :
            null)
        .build();
      }

      ConfigDataV1 configDataV1 = apiConfigCacheClient.getCache(
          List.of(new String[]{"stations", "creditorInstitutions"})
      );

      if (configDataV1.getVersion() == null || configCacheData.getVersion() == null ||
          configDataV1.getVersion().compareTo(configCacheData.getVersion()) >= 0) {
        configCacheData.setConfigDataV1(configDataV1);
        configCacheData.setVersion(configDataV1.getVersion());
        configCacheData.setCacheVersion(cacheUpdateEvent != null &&
            cacheUpdateEvent.getCacheVersion() != null ?
            cacheUpdateEvent.getCacheVersion() : null);
      }

    }

    return this.configCacheData;

  }

}
