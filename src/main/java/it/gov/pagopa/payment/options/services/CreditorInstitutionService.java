package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.CreditorInstitutionRestClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CreditorInstitutionService {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionService.class);

  String APIM_FORWARDER_ENDPOINT = "";

  static String PAYMENT_OPTIONS_SERVICE_SUFFIX = "/payment-options/organizations/%s/notices/%s";

  @Inject
  CreditorInstitutionRestClient creditorInstitutionRestClient;

  public PaymentOptionsResponse getPaymentOptions(
      String idPsp, String idBrokerPsp, String idStazione,
      String noticeNumber, String fiscalCode, Station station) {

    if (station.getConnection().getIp() == null ||
        !station.getConnection().getIp().contains(APIM_FORWARDER_ENDPOINT)) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
          "Station not configured to pass through the APIM Forwarder");
    }

    String endpoint = getEndpoint(station);

    if (station.getVerifyPaymentOptionEndpoint() == null) {
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA,
          "Station new verify endpoint not provided");
    }

    String[] verifyEndpointParts =
        station.getVerifyPaymentOptionEndpoint().split("/",4);
    String targetHost = verifyEndpointParts[0] + verifyEndpointParts[2];
    String[] hostSplit = verifyEndpointParts[2].split(":");
    Long targetPort = hostSplit.length > 1 ?
        Long.parseLong(hostSplit[1]) :
        verifyEndpointParts[0].contains(ProtocolEnum.HTTPS.name().toLowerCase()) ?
          443L : 80L;
    String targetPath = verifyEndpointParts[3].concat(
        String.format(PAYMENT_OPTIONS_SERVICE_SUFFIX, fiscalCode, noticeNumber));


    try {
      return creditorInstitutionRestClient.callEcPaymentOptionsVerify(
          endpoint,
          station.getProxy() != null ? station.getProxy().getProxyHost() : null,
          station.getProxy() != null ? station.getProxy().getProxyPort() : null,
          targetHost, targetPort, targetPath,
          idPsp, idBrokerPsp, idStazione, fiscalCode, noticeNumber
      );
    } catch (MalformedURLException e) {
      logger.error("[Payment Options] Malformed URL: {}", e.getMessage());
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA, e.getMessage());
    }

  }

  private static String getEndpoint(Station station) {
    return (station.getConnection().getProtocol() != null &&
        (station.getConnection().getProtocol().equals(ProtocolEnum.HTTPS)) ?
        ProtocolEnum.HTTPS.name().toLowerCase() :
        station.getConnection().getProtocol().name().toLowerCase()) +
            "://" + station.getConnection().getIp() + ":" +
            (station.getConnection().getPort() != null ?
                String.valueOf(station.getConnection().getPort()) : "80") +
        PAYMENT_OPTIONS_SERVICE_SUFFIX;
  }

}
