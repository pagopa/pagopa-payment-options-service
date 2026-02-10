package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
  
  @Test
  void resolveStationCode_shouldThrowErrorWhenIndexMissing() throws Exception {
	  // no index snapshot -> stationCodeByCiAndSeg is null
	  ConfigCacheData snap = ConfigCacheData.builder()
			  .configDataV1(ConfigDataV1.builder().version("1").build())
			  .stationCodeByCiAndSeg(null)
			  .build();

	  Field f = ConfigCacheService.class.getDeclaredField("cacheRef");
	  f.setAccessible(true);
	  @SuppressWarnings("unchecked")
	  AtomicReference<ConfigCacheData> ref =
	  (AtomicReference<ConfigCacheData>) f.get(configCacheService);

	  ref.set(snap);

	  PaymentOptionsException ex = assertThrows(
			  PaymentOptionsException.class,
			  () -> configCacheService.resolveStationCode("00001", 0L)
			  );
	  assertEquals(AppErrorCodeEnum.ODP_SYSTEM_ERROR, ex.getErrorCode());
  }
  
  @Test
  void resolveStationCode_shouldReturnStationCodeFromIndex() throws Exception {
	  Map<String, Map<Long, String>> index =
			  Map.of("00001", Map.of(0L, "STATION-XYZ"));

	  ConfigCacheData snap = ConfigCacheData.builder()
			  .configDataV1(ConfigDataV1.builder().version("1").build())
			  .stationCodeByCiAndSeg(index)
			  .build();

	  Field f = ConfigCacheService.class.getDeclaredField("cacheRef");
	  f.setAccessible(true);
	  @SuppressWarnings("unchecked")
	  AtomicReference<ConfigCacheData> ref =
	  (AtomicReference<ConfigCacheData>) f.get(configCacheService);

	  ref.set(snap);

	  assertEquals("STATION-XYZ", configCacheService.resolveStationCode("00001", 0L));
	  assertNull(configCacheService.resolveStationCode("00001", 999L));
  }
  
  @Test
  void checkAndUpdateCache_shouldNotDowngradeOnOlderFetchedVersion() {
	  when(apiConfigCacheClient.getCache(any()))
	  .thenReturn(
			  ConfigDataV1.builder().version("10").build(),
			  ConfigDataV1.builder().version("9").build()
			  );

	  configCacheService.checkAndUpdateCache(null);
	  assertEquals("10", configCacheService.getConfigCacheData().getVersion());

	  // force refresh with newest event but fetched version is older -> should NOT downgrade
	  CacheUpdateEvent evt = CacheUpdateEvent.builder().cacheVersion("CACHE").version("999").build();
	  configCacheService.checkAndUpdateCache(evt);

	  // must remain 10 (no downgrade)
	  assertEquals("10", configCacheService.getConfigCacheData().getVersion());
	  verify(apiConfigCacheClient, times(2)).getCache(any());
  }
  
  @Test
  void checkAndUpdateCache_concurrentFirstLoad_shouldCallApiConfigOnlyOnce() throws Exception {
   
    CountDownLatch firstCallEntered = new CountDownLatch(1);
    CountDownLatch releaseRemote = new CountDownLatch(1);

    when(apiConfigCacheClient.getCache(any())).thenAnswer(inv -> {
      firstCallEntered.countDown();              // thread 1 comes here -> I report that it is inside getCache()
      if (!releaseRemote.await(2, TimeUnit.SECONDS)) {
        throw new RuntimeException("timeout waiting test release");
      }
      return ConfigDataV1.builder().version("1").build();
    });

    ExecutorService pool = Executors.newFixedThreadPool(2);

    Callable<ConfigCacheData> task = () -> configCacheService.checkAndUpdateCache(null);

    Future<ConfigCacheData> f1 = pool.submit(task);

    // ensures that the first thread has arrived inside getCache() (so it has acquired refreshLock)
    assertTrue(firstCallEntered.await(2, TimeUnit.SECONDS),
        "First thread did not reach remote call in time");

    // second thread starts while the first is blocked on the remote call -> it should wait for the first to complete and then return the same cached result without calling remote again
    Future<ConfigCacheData> f2 = pool.submit(task);

    // unlocks the remote call, so thread 1 can complete and set cacheRef -> thread 2 should then read the cached value and return
    releaseRemote.countDown();

    ConfigCacheData r1 = f1.get(2, TimeUnit.SECONDS);
    ConfigCacheData r2 = f2.get(2, TimeUnit.SECONDS);

    pool.shutdownNow();

    assertNotNull(r1);
    assertNotNull(r2);
    assertNotNull(r1.getConfigDataV1());
    assertNotNull(r2.getConfigDataV1());
    assertEquals("1", r1.getConfigDataV1().getVersion());
    assertEquals("1", r2.getConfigDataV1().getVersion());

    // only one remote call should have been made, because the second thread should have waited for the first to complete and then read the cached value
    verify(apiConfigCacheClient, times(1)).getCache(any());
  }


}
