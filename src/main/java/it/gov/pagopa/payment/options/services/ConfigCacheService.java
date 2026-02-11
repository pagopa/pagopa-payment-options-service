package it.gov.pagopa.payment.options.services;

import io.quarkus.runtime.StartupEvent;
import it.gov.pagopa.payment.options.clients.ApiConfigCacheClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.StationCreditorInstitution;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.events.CacheUpdateEvent;
import it.gov.pagopa.payment.options.models.ConfigCacheData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

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

	/**
	 * Provides a thread-safe, "all-or-nothing" reference to the cache. 
	 * Readers will always retrieve a fully formed ConfigCacheData object, 
	 * preventing them from seeing partial or corrupted data during an update.
	 */
	private final AtomicReference<ConfigCacheData> cacheRef =
			new AtomicReference<>();

	/**
	 * A mutual exclusion mechanism that ensures only one thread 
	 * can perform the refresh logic at a time. It prevents redundant 
	 * simultaneous updates from multiple triggers like startup or events.
	 */
	private final ReentrantLock refreshLock =
			new ReentrantLock();

	void onStart(@Observes StartupEvent ev) {
		try {
			// Avoid forcing a remote refresh if we already have a valid snapshot.
		    ConfigCacheData current = cacheRef.get();
		    if (current != null && current.getConfigDataV1() != null) {
		      logger.info("[Payment Options] Cache already initialized at startup - skipping refresh");
		      return;
		    }
			getConfigCacheData();
		} catch (Exception e) {
			logger.error("[Payment Options] Encountered error on first cache data retrival: {}", e.getMessage());
		}
	}

	/**
	 * Provides instance of the local cache data, if not yet provided,
	 * it will call the checkAndUpdate method
	 * @return local instance of the configCacheData
	 */
	public ConfigDataV1 getConfigCacheData() {
		// Fast path: return current snapshot without locking.
		ConfigCacheData current = cacheRef.get();
		if (current != null && current.getConfigDataV1() != null) {
			return current.getConfigDataV1();
		}

		// Slow path: refresh is required (e.g., first access or empty snapshot).
		ConfigCacheData updated = checkAndUpdateCache(null);
		if (updated == null || updated.getConfigDataV1() == null) {
			// if after refresh we still don't have data (and no previous snapshot exists), throw an exception to signal the caller.
			throw new PaymentOptionsException(
					AppErrorCodeEnum.ODP_SYSTEM_ERROR,
					"Configuration data currently not available"
					);
		}
		return updated.getConfigDataV1();
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

		// Fast, lock-free check: if cache is present and the event doesn't represent a newer version,
		// return the current snapshot immediately.
		ConfigCacheData current = cacheRef.get();
		if (!needsRefresh(current, cacheUpdateEvent)) {
			return current;
		}

		refreshLock.lock();
		try {
			// Double-check inside the lock to prevent multiple concurrent refreshes.
			current = cacheRef.get();
			if (!needsRefresh(current, cacheUpdateEvent)) {
				return current;
			}

			// Initialize a baseline snapshot if missing (keeps version info even before data is loaded).
			ConfigCacheData base = current;
			if (base == null) {
				base = ConfigCacheData.builder()
					    .cacheVersion(cacheUpdateEvent != null ? cacheUpdateEvent.getCacheVersion() : null)
					    .version(null) // apiVersion is unknown until we fetch the payload, so we keep it null for now.
					    .eventVersion(cacheUpdateEvent != null ? cacheUpdateEvent.getVersion() : null)
					    .build();
			}

			ConfigDataV1 configDataV1 = apiConfigCacheClient.getCache(
					List.of("stations", "creditorInstitutions", "psps", "creditorInstitutionStations", "pspBrokers")
					);

			// Avoid logging the entire payload to prevent huge allocations and GC pressure.
			// Log only high-level sizes and versions.
			logger.info("[Payment Options] api-config cache fetched - apiVersion={}, stations={}, ci={}, psps={}, pspBrokers={}, eventCacheVersion={}, eventVersion={}",
					configDataV1.getVersion(),
					configDataV1.getStations() != null ? configDataV1.getStations().size() : 0,
							configDataV1.getCreditorInstitutions() != null ? configDataV1.getCreditorInstitutions().size() : 0,
									configDataV1.getPsps() != null ? configDataV1.getPsps().size() : 0,
											configDataV1.getPspBrokers() != null ? configDataV1.getPspBrokers().size() : 0,
													cacheUpdateEvent != null ? cacheUpdateEvent.getCacheVersion() : null,
															cacheUpdateEvent != null ? cacheUpdateEvent.getVersion() : null
					);

			// Build a compact index for station resolution and drop the heavy structure afterwards.
			Map<String, Map<Long, String>> stationIndex = buildStationIndex(configDataV1);

			// creditorInstitutionStations is the heaviest collection.
			// After building the index, it is safe to remove it to reduce retained memory.
			configDataV1.setCreditorInstitutionStations(null);
			
			// Determine the version we are currently serving (to prevent downgrades).
			String servedApiVersion = current != null ? current.getVersion() : null;        // apiVersion
			String servedEventVersion = current != null ? current.getEventVersion() : null; // eventVersion

			String incomingCacheVersion = cacheUpdateEvent != null ? cacheUpdateEvent.getCacheVersion() : null;
			String incomingEventVersion = cacheUpdateEvent != null ? cacheUpdateEvent.getVersion() : null;

			String fetchedApiVersion = configDataV1.getVersion(); // apiVersion fetched (actually null in DEV/UAT)
			
			// Guard 1: If we have an event and it's not newer than the one already applied (same stream), DO NOT apply anything.
			if (cacheUpdateEvent != null
			    && current != null
			    && incomingCacheVersion != null
			    && incomingCacheVersion.equals(current.getCacheVersion())
			    && !isNewer(incomingEventVersion, servedEventVersion)) {
			  return current;
			}
			
			// Guard 2 (future-proof): if apiVersion is provided, prevent downgrade on content.
			boolean apiOk = isNewerOrEqual(fetchedApiVersion, servedApiVersion);

			// Update snapshot only if fetched version is >= served version (or served version is null).
			if (apiOk) {
				ConfigCacheData newSnapshot = ConfigCacheData.builder()
						.cacheVersion(incomingCacheVersion != null ? incomingCacheVersion : base.getCacheVersion())
						// version = apiVersion (if it exists), otherwise keep the previous one
						.version(fetchedApiVersion != null ? fetchedApiVersion : servedApiVersion)
						// eventVersion = event version (if there is an event), otherwise keep the previous one
						.eventVersion(incomingEventVersion != null ? incomingEventVersion : servedEventVersion)
						.configDataV1(configDataV1)
						.stationCodeByCiAndSeg(stationIndex)
						.build();

				cacheRef.set(newSnapshot);
				return newSnapshot;
			}

			logger.warn("[Payment Options] Ignoring fetched cache payload because it would downgrade apiVersion (served={}, fetched={})",
				    servedApiVersion, fetchedApiVersion);

			// Fetched payload is older than what we are currently serving -> DO NOT downgrade.
			// Keep serving the current snapshot if present, otherwise fallback to base.
			return (current != null) ? current : base;

		} catch (Exception e) {
			logger.error("[Payment Options] Error updating api-config cache: {}", e.getMessage(), e);

			// If exist a valid snapshot, keep serving it.
			if (current != null && current.getConfigDataV1() != null) {
				return current;
			}

			// No valid snapshot available -> propagate error.
			throw e ;
		} finally {
			refreshLock.unlock();
		}
	}
	
	private boolean needsRefresh(ConfigCacheData current, CacheUpdateEvent evt) {
		// If no snapshot exists, must refresh.
		if (current == null) return true;

		// refresh only if we don't have a valid payload yet (prevents re-downloading the whole cache on every access)
		if (evt == null) {
			return current.getConfigDataV1() == null;
		}

		// if I have no payload, I need to refresh to get it, regardless of the event version (prevents blocking updates when version info is missing).
		if (current.getConfigDataV1() == null) return true;

		// different stream => refresh
		if (current.getCacheVersion() == null) return true;
		if (evt.getCacheVersion() == null || !evt.getCacheVersion().equals(current.getCacheVersion())) return true;

		// newest event => refresh
		return isNewer(evt.getVersion(), current.getEventVersion());
	}



	private Map<String, Map<Long, String>> buildStationIndex(ConfigDataV1 data) {
		// Transforms the "creditorInstitutionStations" payload into a compact index:
		// creditorInstitutionCode -> (segregationCode -> stationCode)
		// This enables O(1) station resolution at runtime and allows us to drop the heavy map.
		java.util.Map<String, StationCreditorInstitution> map = data.getCreditorInstitutionStations();
		if (map == null || map.isEmpty()) {
			return java.util.Collections.emptyMap();
		}

		HashMap<String, java.util.Map<Long, String>> out = new HashMap<>();
		for (StationCreditorInstitution sci : map.values()) {
			if (sci == null ||
					sci.getCreditorInstitutionCode() == null ||
					sci.getSegregationCode() == null ||
					sci.getStationCode() == null) {
				continue;
			}

			out.computeIfAbsent(sci.getCreditorInstitutionCode(), k -> new HashMap<>())
			.put(sci.getSegregationCode(), sci.getStationCode());
		}
		return out;
	}
	
	public String resolveStationCode(String creditorInstitutionCode, long segregationCode) {
		// Runtime O(1) lookup using the compact index built at refresh time.
		// If the index is missing, we fail fast to avoid inconsistent results.
		ConfigCacheData snap = cacheRef.get();
		if (snap == null || snap.getStationCodeByCiAndSeg() == null) {
			throw new PaymentOptionsException(
					AppErrorCodeEnum.ODP_SYSTEM_ERROR,
					"Configuration data currently not available"
					);
		}

		java.util.Map<Long, String> bySeg = snap.getStationCodeByCiAndSeg().get(creditorInstitutionCode);
		return bySeg != null ? bySeg.get(segregationCode) : null;
	}
	/*
	private boolean isNewerOrEqual(String fetchedVersion, String servedVersion) {
		// If we don't know one of the versions, assume we need to refresh to be safe (prevents blocking updates when version info is missing).
		if (fetchedVersion == null || servedVersion == null) {
			return true;
		}

		// Numeric compare if both versions are numeric (e.g., timestamps or simple version numbers).
		try {
			long fetched = Long.parseLong(fetchedVersion);
			long served = Long.parseLong(servedVersion);
			return fetched >= served;
		} catch (NumberFormatException ignored) {
			// Fallback to lexicographical comparison if versions are not numeric.
			return fetchedVersion.compareTo(servedVersion) >= 0;
		}
	}*/
	
	private int compareVersions(String a, String b) {
		if (a == null && b == null) return 0;
		if (a == null) return -1;
		if (b == null) return 1;

		try {
			return new java.math.BigInteger(a).compareTo(new java.math.BigInteger(b));
		} catch (NumberFormatException e) {
			return a.compareTo(b); // fallback
		}
	}

	private boolean isNewer(String a, String b) {
		return compareVersions(a, b) > 0;
	}

	private boolean isNewerOrEqual(String a, String b) {
		return compareVersions(a, b) >= 0;
	}


}
