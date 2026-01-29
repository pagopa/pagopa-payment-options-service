package it.gov.pagopa.payment.options.consumers;

import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
  public void consume(CacheUpdateEvent event) {

	  if (event == null) {
		  // if cannot receive a valid event, just skip it.
		  logger.warn("[Payment Options] Received null cache update event - skipping");
		  return;
	  }

	  logger.info("[Payment Options] Received update event with cacheVersion {}"
			  + " and version {}", event.getCacheVersion(), event.getVersion());
	  try {
		  configCacheService.checkAndUpdateCache(event);
	  } catch (Exception e) {
		  // Best-effort cache update:
		  // If an update fails we must NOT rethrow here. Rethrowing can trigger retries / message reprocessing
		  // depending on the reactive messaging failure strategy, causing CPU/memory spikes and pod restarts.
		  // By swallowing the exception, the service keeps running and continues serving requests using
		  // the last known good cache snapshot, and the next Kafka event will try to refresh again.	
		  logger.error(
				  "[Payment Options] Cache update failed (cacheVersion={}, version={}). Keeping previous snapshot. Cause: {}",
				  event.getCacheVersion(),
				  event.getVersion(),
				  e.getMessage(),
				  e
				  );
	  }

  }

}
