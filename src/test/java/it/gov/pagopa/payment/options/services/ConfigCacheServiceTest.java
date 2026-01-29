package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigCacheServiceTest {

  @Mock
  ApiConfigCacheClient apiConfigCacheClient;

  ConfigCacheService configCacheService;

  @BeforeEach
  void setup() throws Exception {
    configCacheService = new ConfigCacheService();

    // inject mock into the service using reflection
    Field f = ConfigCacheService.class.getDeclaredField("apiConfigCacheClient");
    f.setAccessible(true);
    f.set(configCacheService, apiConfigCacheClient);
  }

  @Test
  void firstLoadFails_shouldPropagate() {
    when(apiConfigCacheClient.getCache(any()))
        .thenThrow(new RuntimeException("test"));

    assertThrows(RuntimeException.class,
        () -> configCacheService.checkAndUpdateCache(null));

    verify(apiConfigCacheClient, times(1)).getCache(any());
  }

  @Test
  void refreshFails_shouldKeepLastKnownGood() {
    // first load ok -> snapshot version=1
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(ConfigDataV1.builder().version("1").build());

    assertDoesNotThrow(() -> configCacheService.checkAndUpdateCache(null));
    assertEquals("1", configCacheService.getConfigCacheData().getVersion());

    // next refresh fails -> should NOT throw, must keep version=1
    when(apiConfigCacheClient.getCache(any()))
        .thenThrow(new RuntimeException("boom"));

    CacheUpdateEvent evt = CacheUpdateEvent.builder()
        .cacheVersion("CACHE")
        .version("2")
        .build();

    assertDoesNotThrow(() -> configCacheService.checkAndUpdateCache(evt));
    assertEquals("1", configCacheService.getConfigCacheData().getVersion());

    verify(apiConfigCacheClient, times(2)).getCache(any());
  }
  
  @Test
  void checkAndUpdateCacheOK() {
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(ConfigDataV1.builder().version("2").build());

    // 1) First load with no event -> must fetch
    configCacheService.checkAndUpdateCache(null);

    ConfigDataV1 configCacheData = configCacheService.getConfigCacheData();
    assertNotNull(configCacheData);
    assertEquals("2", configCacheData.getVersion());

    // 2) Event arrives with older version, but cacheVersion was still null -> refresh happens once
    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE").version("1").build()
    );

    configCacheData = configCacheService.getConfigCacheData();
    assertNotNull(configCacheData);
    assertEquals("2", configCacheData.getVersion());

    // 3) Same event again -> must NOT refresh again
    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE").version("1").build()
    );

    verify(apiConfigCacheClient, times(2)).getCache(any());
  }
  
  @Test
  void checkAndUpdateCache_shouldRefreshOnCacheVersionMismatch() {
    // First fetch -> version 2, second fetch -> version 3
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(
            ConfigDataV1.builder().version("2").build(),
            ConfigDataV1.builder().version("3").build()
        );

    // 1) First load without event
    configCacheService.checkAndUpdateCache(null);
    assertNotNull(configCacheService.getConfigCacheData());
    assertEquals("2", configCacheService.getConfigCacheData().getVersion());

    // 2) First event sets cacheVersion = CACHE-A (forces refresh because current.cacheVersion was null)
    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE-A").version("1").build()
    );
    assertEquals("3", configCacheService.getConfigCacheData().getVersion());

    // 3) Send an event with DIFFERENT cacheVersion: must refresh again (mismatch)
    reset(apiConfigCacheClient);
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(ConfigDataV1.builder().version("4").build());

    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE-B").version("1").build()
    );

    assertEquals("4", configCacheService.getConfigCacheData().getVersion());
    verify(apiConfigCacheClient, times(1)).getCache(any());
  }
  
  @Test
  void checkAndUpdateCache_shouldRefreshWhenEventVersionIsNewer() {
    // First fetch -> version 2, second fetch -> version 3
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(
            ConfigDataV1.builder().version("2").build(),
            ConfigDataV1.builder().version("3").build()
        );

    // 1) First load
    configCacheService.checkAndUpdateCache(null);
    assertEquals("2", configCacheService.getConfigCacheData().getVersion());

    // 2) Event sets cacheVersion and (older) event version -> refresh once due to cacheVersion null
    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE").version("1").build()
    );
    assertEquals("3", configCacheService.getConfigCacheData().getVersion());

    // 3) Now we have snapshot cacheVersion=CACHE and version=3. Send an event with same cacheVersion but a NEWER version => must refresh.
    reset(apiConfigCacheClient);
    when(apiConfigCacheClient.getCache(any()))
        .thenReturn(ConfigDataV1.builder().version("4").build());

    configCacheService.checkAndUpdateCache(
        CacheUpdateEvent.builder().cacheVersion("CACHE").version("999").build()
    );

    assertEquals("4", configCacheService.getConfigCacheData().getVersion());
    verify(apiConfigCacheClient, times(1)).getCache(any());
  }



}
