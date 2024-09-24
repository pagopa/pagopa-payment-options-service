package it.gov.pagopa.payment.options.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Service to manage local instance of the config cache, to contain and update
 * based on retrieved data
 */
@ApplicationScoped
public class ConfigCacheService {

  private final Logger logger = LoggerFactory.getLogger(ConfigCacheService.class);

  @Inject
  @RestClient
  public ApiConfigCacheClient apiConfigCacheClient;

  private ConfigCacheData configCacheData;

  /**
   * Provides instance of the local cache data, if not yet provided,
   * it will call the checkAndUpdate method
   * @return local instance of the configCacheData
   */
  public ConfigDataV1 getConfigCacheData() {
    return this.configCacheData != null || this.configCacheData.getConfigDataV1() == null ?
        this.configCacheData.getConfigDataV1() :
        checkAndUpdateCache(null).getConfigDataV1();
  }

  /**
   * Executes a check and update process based on the update event (if provided). If the
   * input event is null it will always execute a call using the client instance
   *
   * If the event is provided, it will check if the version is obsolete, and will execute
   * the update only when a new cache version is passed through the update event
   *
   * @param cacheUpdateEvent contains version of the update event
   * @return instance of the configCacheData
   */
  @SneakyThrows
  public ConfigCacheData checkAndUpdateCache(CacheUpdateEvent cacheUpdateEvent) {

    if (configCacheData == null || cacheUpdateEvent == null ||
          configCacheData.getVersion() == null ||
          configCacheData.getCacheVersion() == null ||
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

      logger.debug("[Payment Options] Retrieved data {}",
          new ObjectMapper().writeValueAsString(configDataV1));

      if (configDataV1.getVersion() == null || configCacheData.getVersion() == null ||
          configDataV1.getVersion().compareTo(configCacheData.getVersion()) >= 0) {
        configCacheData.setConfigDataV1(configDataV1);
        if (configDataV1.getVersion() != null) {
          configCacheData.setVersion(configDataV1.getVersion());
        }
        configCacheData.setCacheVersion(cacheUpdateEvent != null &&
            cacheUpdateEvent.getCacheVersion() != null ?
            cacheUpdateEvent.getCacheVersion() : null);
      }

    }

    return this.configCacheData;

  }

}
