package it.gov.pagopa.payment.options.consumers;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.models.CacheUpdateEvent;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ConfigCacheUpdatesConsumerTest {

  @InjectMock
  ConfigCacheService configCacheService;

  @Inject
  ConfigCacheUpdatesConsumer configCacheUpdatesConsumer;

  @BeforeEach
  public void init() {
    Mockito.reset(configCacheService);
  }

  @Test
  void consumeWithOk() {
    when(configCacheService.checkAndUpdateCache(any()))
        .thenReturn(ConfigCacheData.builder().build());
    configCacheUpdatesConsumer.consume(CacheUpdateEvent.builder()
            .cacheVersion("CACHE")
            .version("11221")
            .timestamp("12121212")
        .build());
    verify(configCacheService).checkAndUpdateCache(any());
  }

  @Test
  void consumeWithKo() {
    when(configCacheService.checkAndUpdateCache(any()))
        .thenThrow(new RuntimeException());
    assertThrows(Exception.class, () -> configCacheUpdatesConsumer.consume(CacheUpdateEvent.builder()
        .cacheVersion("CACHE")
        .version("11221")
        .timestamp("12121212")
        .build()));
    verify(configCacheService).checkAndUpdateCache(any());
  }

}
