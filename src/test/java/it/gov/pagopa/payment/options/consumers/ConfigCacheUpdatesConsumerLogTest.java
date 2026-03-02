package it.gov.pagopa.payment.options.consumers;

import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConfigCacheUpdatesConsumerLogTest {

  private ConfigCacheUpdatesConsumer consumer;
  private ConfigCacheService configCacheService;
  private Logger slf4jLogger;

  @BeforeEach
  void setUp() throws Exception {
    consumer = new ConfigCacheUpdatesConsumer();

    // inject mocked service
    configCacheService = mock(ConfigCacheService.class);
    consumer.configCacheService = configCacheService;

    slf4jLogger = mock(Logger.class);
    Field f = ConfigCacheUpdatesConsumer.class.getDeclaredField("logger");
    f.setAccessible(true);
    f.set(consumer, slf4jLogger);
  }

  @Test
  void consume_withKo_shouldLogErrorAndNotThrow() {
	  when(configCacheService.checkAndUpdateCache(any()))
	  .thenThrow(new RuntimeException("exception"));

	  CacheUpdateEvent event = CacheUpdateEvent.builder()
			  .cacheVersion("CACHE")
			  .version("11221")
			  .timestamp("12121212")
			  .build();

	  assertDoesNotThrow(() -> consumer.consume(event));

	  verify(configCacheService).checkAndUpdateCache(any());

	  verify(slf4jLogger).error(
			  eq("[Payment Options] Cache update failed (cacheVersion={}, version={}). Keeping previous snapshot. Cause: {}"),
			  eq("CACHE"),
			  eq("11221"),
			  eq("exception"),
			  any(RuntimeException.class)
			  );
  }
}