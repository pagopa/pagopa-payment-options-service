package it.gov.pagopa.payment.options.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerPsp;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.CreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.PaymentServiceProvider;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.events.odpRe.EventType;
import it.gov.pagopa.payment.options.models.events.odpRe.Status;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains services to manage payment options
 */
@ApplicationScoped
public class PaymentOptionsService {

  private final Logger logger = LoggerFactory.getLogger(PaymentOptionsService.class);

  private final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
      .withZone(ZoneId.systemDefault());

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  ConfigCacheService configCacheService;

  @Inject
  CreditorInstitutionService creditorInstitutionService;

  @Inject
  EventService eventService;

  /**
   * Provides the service method to execute payment options verify process, attempting
   * to use the extracted station config data to contact the exposed creditor institution
   * Rest client
   * @param idPsp input id PSP
   * @param idBrokerPsp input id Broker PSP
   * @param fiscalCode EC fiscal code
   * @param noticeNumber input notice number
   * @return instance of extracted PaymentOptions, obtained from the external creditor institution
   * REST api
   */
  public PaymentOptionsResponse getPaymentOptions(
      String idPsp, String idBrokerPsp, String fiscalCode, String noticeNumber, String sessionId) {

    Station station = null;
    long segregationCode;
    
    String creditorInstitutionCode = null;
    String stationCode = null;

    try {

      validateInput(idPsp, idBrokerPsp, fiscalCode, noticeNumber);

      long auxDigit = Long.parseLong(noticeNumber.substring(0, 1));
      if (auxDigit != 3) {
        throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_NAV_NOT_NMU,
            "Notice number contains a nav not valid for the OdP service");
      }

      segregationCode = Long.parseLong(noticeNumber.substring(1, 3));

      ConfigDataV1 configCacheData = getConfigData();
      // Validate PSP / Broker. This allows to fail fast in case of missing/invalid configuration.
      getAndValidatePsp(idPsp, configCacheData);
      getAndValidateBrokerPsp(idBrokerPsp, configCacheData);
      
      Map<String, Station> stationMap = configCacheData.getStations();
      if (stationMap == null) {
        throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
            "Configuration data currently not available");
      }
      
      // Resolve creditor institution: keep only primitive codes to minimize retained memory.
      CreditorInstitution creditorInstitution = getCreditorInstitution(fiscalCode, configCacheData);
      creditorInstitutionCode = creditorInstitution.getCreditorInstitutionCode();
      
      // stationCode is resolved through the compact index built by ConfigCacheService.
      // This avoids scanning large collections and keeps memory/GC pressure low.
      stationCode = configCacheService.resolveStationCode(creditorInstitutionCode, segregationCode);

      if (stationCode == null) {
    	  throw new PaymentOptionsException(
    			  AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA,
    			  "Station related to the creditor institution not found"
    			  );
      }

      station = stationMap.get(stationCode);

      
      if (station == null) {
    	  throw new PaymentOptionsException(
    	      AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA,
    	      "Station not found using station code " + stationCode
    	  );
    	} else if (!Boolean.TRUE.equals(station.getEnabled())) {
    	  throw new PaymentOptionsException(
    	      AppErrorCodeEnum.ODP_STAZIONE_INT_PA_DISABILITATA,
    	      "Station found using station code " + stationCode + " disabled"
    	  );
    	} else if (!Boolean.TRUE.equals(station.getVerifyPaymentOptionEnabled())) {
    	  throw new PaymentOptionsException(
    	      AppErrorCodeEnum.ODP_STAZIONE_INT_VERIFICA_ODP_DISABILITATA,
    	      "Station found using station code " + stationCode
    	          + " has the OdP verify service disabled. Use the standard verification flow"
    	  );
    	}

    } catch (PaymentOptionsException e) {
      Instant instantForPspReq = Instant.now();
      sendKoEvent(idPsp, idBrokerPsp, fiscalCode, noticeNumber, stationCode, creditorInstitutionCode,
    		    instantForPspReq, e);
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station != null ? station.getStationCode() : null, sessionId, LocalDateTime
              .ofInstant(instantForPspReq, ZoneId.systemDefault())
              .format(formatter),
          Status.KO, EventType.REQ, null,
          e.getErrorCode().getErrorCode(), e.getMessage()
      );
      throw e;
    }

    try {

      Instant instantForPspReq = Instant.now();
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instantForPspReq, ZoneId.systemDefault())
              .format(formatter),
          Status.OK, EventType.REQ,
          null, null, null
      );

      Instant instantForEcReq = Instant.now();
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instantForEcReq, ZoneId.systemDefault())
              .format(formatter),
          Status.OK, EventType.REQ,
          null, null, null
      );

      PaymentOptionsResponse paymentOptionsResponse =
          creditorInstitutionService.getPaymentOptions(noticeNumber, fiscalCode, station, segregationCode);


      Instant instantForEcRes = Instant.now();
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instantForEcRes, ZoneId.systemDefault())
              .format(formatter),
          Status.OK, EventType.RES,
          objectMapper.writeValueAsString(paymentOptionsResponse)
          , null, null);

      Instant instantForPspRes = Instant.now();
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instantForPspRes, ZoneId.systemDefault())
              .format(formatter),
          Status.OK, EventType.RES,
          objectMapper.writeValueAsString(paymentOptionsResponse),
          null, null
      );

      return paymentOptionsResponse;

    } catch (CreditorInstitutionException e) {
      logger.error("[Payment Options] encountered a managed error: {}", e.getMessage());
      Instant instant = Instant.now();
      try {
        eventService.sendEvent(
            idPsp, idBrokerPsp, noticeNumber, fiscalCode,
            station.getStationCode(), sessionId, LocalDateTime
                .ofInstant(instant, ZoneId.systemDefault())
                .format(formatter),
            Status.KO, EventType.RES,
            objectMapper.writeValueAsString(e.getErrorResponse()),
            e.getErrorResponse().getAppErrorCode(),
            e.getMessage()
        );
      } catch (JsonProcessingException ex) {
        throw new RuntimeException(ex);
      }
    
      eventService.sendVerifyKoEvent(
    		    idPsp, idBrokerPsp, noticeNumber, fiscalCode,
    		    station.getStationCode(),
    		    creditorInstitutionCode,
    		    e.getErrorResponse().getAppErrorCode(),
    		    e.getErrorResponse().getErrorMessage(),
    		    instant.getEpochSecond(),
    		    LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter)
    		);

      throw e;
    } catch (PaymentOptionsException e) {
      logger.error("[Payment Options] encountered a managed error: {}", e.getMessage());
      Instant instant = Instant.now();
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instant, ZoneId.systemDefault())
              .format(formatter),
          Status.KO, EventType.REQ,
          null, e.getErrorCode().getErrorCode(), e.getMessage()
      );
      sendKoEvent(idPsp, idBrokerPsp, fiscalCode, noticeNumber, stationCode, creditorInstitutionCode,
          instant, e);
      throw e;
    } catch (Exception e) {
      logger.error("[Payment Options] encountered an unexpected error: {}", e.getMessage());
      Instant instantForEcRes = Instant.now();
      PaymentOptionsException paymentOptionsException =
          new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
              "Encountered an unmanaged error during payment option retrieval");
      eventService.sendEvent(
          idPsp, idBrokerPsp, noticeNumber, fiscalCode,
          station.getStationCode(), sessionId, LocalDateTime
              .ofInstant(instantForEcRes, ZoneId.systemDefault())
              .format(formatter),
          Status.KO, EventType.REQ, null,
          paymentOptionsException.getErrorCode().getErrorCode(),
          paymentOptionsException.getMessage()
      );
      sendKoEvent(idPsp, idBrokerPsp, fiscalCode, noticeNumber, stationCode, creditorInstitutionCode,
          instantForEcRes, paymentOptionsException);
      throw paymentOptionsException;
    }

  }

  private ConfigDataV1 getConfigData() {
    ConfigDataV1 configCacheData;
    try {
      configCacheData = configCacheService.getConfigCacheData();
    } catch (Exception e) {
      logger.error("[Payment Options] Unexpected error recovering configuration data: {}",
          e.getMessage());
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }
    if (configCacheData == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }
    return configCacheData;
  }

  private static void validateInput(
      String idPsp, String idBrokerPsp, String fiscalCode, String noticeNumber) {
    if (idPsp == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SINTASSI,
          "Missing input idPsp");
    }
    if (idBrokerPsp == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SINTASSI,
          "Missing input idBrokerPsp");
    }
    if (fiscalCode == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SINTASSI,
          "Missing input fiscalCode");
    }
    if (noticeNumber == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SINTASSI,
          "Missing input noticeNumber");
    }
  }
  
  private void sendKoEvent(
		  String idPsp, String idBrokerPsp, String fiscalCode, String noticeNumber,
		  String stationCode, String creditorInstitutionCode,
		  Instant instant, PaymentOptionsException e) {

	  logger.error("[Payment Options] encountered a managed error: {}", e.getMessage());

	  String formattedDateTime = LocalDateTime
			  .ofInstant(instant, ZoneId.systemDefault())
			  .format(formatter);

	  eventService.sendVerifyKoEvent(
			  idPsp, idBrokerPsp, noticeNumber, fiscalCode,
			  stationCode,
			  creditorInstitutionCode,
			  e.getErrorCode().getErrorCode(),
			  e.getErrorCode().getErrorMessage(),
			  instant.getEpochSecond(),
			  formattedDateTime);
  }
  
  private static CreditorInstitution getCreditorInstitution(String fiscalCode, ConfigDataV1 configCacheData) {
	  // Extract and validate creditor institution from the config cache
	  Map<String, CreditorInstitution> creditorInstitutionMap = configCacheData.getCreditorInstitutions();
	  if (creditorInstitutionMap == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
				  "Configuration data currently not available");
	  }

	  CreditorInstitution creditorInstitution = creditorInstitutionMap.get(fiscalCode);
	  if (creditorInstitution == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_DOMINIO_SCONOSCIUTO,
				  "Creditor institution with id " + fiscalCode + " not found");
	  } else if (!Boolean.TRUE.equals(creditorInstitution.getEnabled())) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_DOMINIO_DISABILITATO,
				  "Creditor institution with id " + fiscalCode + " disabled");
	  }

	  if (creditorInstitution.getCreditorInstitutionCode() == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
				  "Creditor institution code missing");
	  }

	  return creditorInstitution;
  }
  
  private static PaymentServiceProvider getAndValidatePsp(String idPsp, ConfigDataV1 configCacheData) {
	  // Validate PSP data using the config cache.
	  Map<String, PaymentServiceProvider> psps = configCacheData.getPsps();
	  if (psps == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
				  "Configuration data currently not available");
	  }

	  PaymentServiceProvider psp = psps.get(idPsp);
	  if (psp == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_SCONOSCIUTO,
				  "PSP with id " + idPsp + " not found");
	  }

	  if (!psp.isEnabled()) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_DISABILITATO,
				  "PSP with id " + idPsp + " disabled");
	  }

	  return psp;
  }

  private static BrokerPsp getAndValidateBrokerPsp(String idBrokerPsp, ConfigDataV1 configCacheData) {
	  // Validate broker PSP.
	  Map<String, BrokerPsp> brokers = configCacheData.getPspBrokers();
	  if (brokers == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
				  "Configuration data currently not available");
	  }

	  BrokerPsp broker = brokers.get(idBrokerPsp);
	  if (broker == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_SCONOSCIUTO,
				  "PSP Broker with id " + idBrokerPsp + " not found");
	  }

	  if (!broker.isEnabled()) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_DISABILITATO,
				  "PSP Broker with id " + idBrokerPsp + " disabled");
	  }

	  return broker;
  }

}
