package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerPsp;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.CreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.PaymentServiceProvider;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.cache.StationCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains services to manage payment options
 */
@ApplicationScoped
public class PaymentOptionsService {

  private final Logger logger = LoggerFactory.getLogger(PaymentOptionsService.class);

  @Inject
  ConfigCacheService configCacheService;

  @Inject
  CreditorInstitutionService creditorInstitutionService;

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

    long auxDigit = Long.parseLong(noticeNumber.substring(0, 1));
    if (auxDigit != 3) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_NAV_NOT_NMU,
          "Notice number contains a nav not valid for the OdP service");
    }

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

    Map<String, PaymentServiceProvider> paymentOptionsServiceMap = configCacheData.getPsps();
    if (paymentOptionsServiceMap == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }

    PaymentServiceProvider paymentServiceProvider = paymentOptionsServiceMap.get(idPsp);
    if (paymentServiceProvider == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_SCONOSCIUTO,
          "PSP with id " + idPsp + " not found");
    } else if (!paymentServiceProvider.isEnabled()) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_PSP_DISABILITATO,
          "PSP with id " + idPsp + " disabled");
    }

    Map<String, BrokerPsp> pspBrokers = configCacheData.getPspBrokers();
    if (pspBrokers == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }

    BrokerPsp brokerPsp = pspBrokers.get(idBrokerPsp);
    if (brokerPsp == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_SCONOSCIUTO,
          "PSP Broker with id " + idBrokerPsp + " not found");
    } else if (!brokerPsp.isEnabled()) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_DISABILITATO,
          "PSP Broker with id " + idBrokerPsp + " disabled");
    }

    Map<String, CreditorInstitution> creditorInstitutionMap =
        configCacheData.getCreditorInstitutions();
    if (creditorInstitutionMap == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }

    CreditorInstitution creditorInstitution = creditorInstitutionMap.get(fiscalCode);
    if (creditorInstitution == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_DOMINIO_SCONOSCIUTO,
          "PSP Broker with id " + idBrokerPsp + " not found");
    } else if (!creditorInstitution.getEnabled()) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_DOMINIO_DISABILITATO,
          "PSP Broker with id " + idBrokerPsp + " not found");
    }

    Map<String, StationCreditorInstitution> stationCreditorInstitutionMap =
        configCacheData.getCreditorInstitutionStations();
    if (stationCreditorInstitutionMap == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }

    StationCreditorInstitution stationCreditorInstitution =
        stationCreditorInstitutionMap.values().stream().filter(item ->
        item.getAuxDigit().equals(auxDigit) &&
        item.getCreditorInstitutionCode().equals(creditorInstitution.getCreditorInstitutionCode()))
        .findFirst().orElseThrow(() ->
                new PaymentOptionsException(
                    AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA,
                    "Station related to the creditor institution not found"));

    Map<String, Station> stationMap = configCacheData.getStations();
    if (stationMap == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Configuration data currently not available");
    }


    Station station = stationMap.get(stationCreditorInstitution.getStationCode());
    if (station == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA,
          "Station not found using station code " + stationCreditorInstitution.getStationCode());
    } else if (!station.getEnabled()) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_DISABILITATA,
          "Station found using station code " +
              stationCreditorInstitution.getStationCode() + " disabled");
    } else if (!station.getVerifyPaymentOptionEnabled()) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_STAZIONE_INT_VERIFICA_ODP_DISABILITATA,
          "Station found using station code " +
              stationCreditorInstitution.getStationCode() + "has the OdP verify service disabled."
              + " Use the standard verification flow");
    }

    try {
      return creditorInstitutionService.getPaymentOptions(noticeNumber, fiscalCode, station);
    } catch (CreditorInstitutionException | PaymentOptionsException e) {
      logger.error("[Payment Options] encountered a managed error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR,
          "Encountered an unmanaged error during payment option retrieval");
    }

  }

}
