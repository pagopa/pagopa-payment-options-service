package it.gov.pagopa.payment.options.consumers;

import it.gov.pagopa.payment.options.models.CacheUpdateEvent;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer for events coming from the nodo-dei-pagamenti-cache topic, triggering
 * the process for local cache-config update
 */
@ApplicationScoped
public class ConfigCacheUpdatesConsumer {

  private final Logger logger = LoggerFactory.getLogger(ConfigCacheUpdatesConsumer.class);

  @Inject
  public ConfigCacheService configCacheService;

  /**
   * Consume method, containing the trigger of the local cache config update
   * @param event cache update event
   */
  @Incoming("nodo-dei-pagamenti-cache")
  @Transactional
  public void consume(CacheUpdateEvent event) {

    logger.info("[Payment Options] Received update event with cacheVersion {}"
        + " and version {}", event.getCacheVersion(), event.getVersion());
    try {
      configCacheService.checkAndUpdateCache(event);
    } catch (Exception e) {
      logger.error("[Payment Options] Error occurred during cache update: {}", e.getMessage());
      throw e;
    }

  }

}
