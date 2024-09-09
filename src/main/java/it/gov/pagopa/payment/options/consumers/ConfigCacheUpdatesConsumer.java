package it.gov.pagopa.payment.options.consumers;

import it.gov.pagopa.payment.options.models.CacheUpdateEvent;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class ConfigCacheUpdatesConsumer {

  @Inject
  public ConfigCacheService configCacheService;

  @Incoming("nodo-dei-pagamenti-cache")
  @Transactional
  public void consume(CacheUpdateEvent event) {
    configCacheService.checkAndUpdateCache(event);
  }

}
