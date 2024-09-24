package it.gov.pagopa.payment.options.services;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ConfigCacheServiceTest {

  @InjectMock
  @RestClient
  private ApiConfigCacheClient apiConfigCacheClient;

  @Inject
  private ConfigCacheService configCacheService;

  @BeforeEach
  public void init() {
    Mockito.reset(apiConfigCacheClient);
  }

  @Test
  void checkAndUpdateCacheKO() {
    when(apiConfigCacheClient.getCache(any())).thenThrow(new RuntimeException("test"));
    assertThrows(Exception.class, () -> configCacheService.checkAndUpdateCache(null));
  }

  @Test
  void checkAndUpdateCacheOk() {
    when(apiConfigCacheClient.getCache(any())).thenReturn(
        ConfigDataV1.builder().version("2").build()
    );
    configCacheService.checkAndUpdateCache(null);
    ConfigDataV1 configCacheData = configCacheService.getConfigCacheData();
    assertNotNull(configCacheData);
    assertEquals(configCacheData.getVersion(), "2");
    configCacheService.checkAndUpdateCache(CacheUpdateEvent.builder()
        .cacheVersion("CACHE").version("1").build());
    configCacheData = configCacheService.getConfigCacheData();
    assertNotNull(configCacheData);
    assertEquals(configCacheData.getVersion(), "2");
    configCacheService.checkAndUpdateCache(CacheUpdateEvent.builder()
        .cacheVersion("CACHE").version("1").build());
    verify(apiConfigCacheClient, times(2)).getCache(any());
  }

}
