package it.gov.pagopa.payment.options.models;

import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCacheData {

	private String cacheVersion;
	// version declared by the api-config-cache payload (ConfigDataV1.version)
	private String version;
	// Kafka event version (CacheUpdateEvent.version)
	private String eventVersion;
	private ConfigDataV1 configDataV1;

	//Compact index to resolve stationCode without retaining the creditorInstitutionStations payload.
	//Structure: creditorInstitutionCode -> (segregationCode -> stationCode)
	private java.util.Map<String, java.util.Map<Long, String>> stationCodeByCiAndSeg;
}
