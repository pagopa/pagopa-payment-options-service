package it.gov.pagopa.payment.options.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;

@ApplicationScoped
public class CreditorInstitutionRestClient {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionRestClient.class);

  @Inject
  ObjectMapper objectMapper;

  public PaymentOptionsResponse callEcPaymentOptionsVerify(
      String endpoint, String proxyHost, Long proxyPort,
      String targetHost, Long targetPort, String targetPath,
      String idPsp, String idBrokerPsp,
      String idStazione, String fiscalCode, String noticeNumber)
      throws MalformedURLException {

    RestClientBuilder builder =
        RestClientBuilder.newBuilder().baseUrl(new URL(endpoint));
    if (proxyHost != null && proxyPort != null) {
      builder = builder.proxyAddress(proxyHost, proxyPort.intValue());
    }
    CreditorInstitutionRestClientInterface ecRestClientInterface = builder.build(
        CreditorInstitutionRestClientInterface.class);

    try (Response response = ecRestClientInterface.verifyPaymentOptions(
        fiscalCode, noticeNumber, idPsp, idBrokerPsp, idStazione,
        targetHost, targetPort.intValue(), targetPath)) {

      if (response.getStatus() != 200) {
        manageErrorResponse(response);
      }

      return objectMapper.readValue(
          response.readEntity(String.class), PaymentOptionsResponse.class);

    } catch (ClientWebApplicationException clientWebApplicationException) {
      logger.error("[Payment Options] Encountered REST client exception: {}",
          clientWebApplicationException.getMessage());
      manageErrorResponse(clientWebApplicationException.getResponse());
      return null;
    } catch (Exception e) {
      logger.error("[Payment Options] Unable to call the station due to error: {}",
          e.getMessage());
      throw new PaymentOptionsException(
          AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, e.getMessage());
    }

  }

  private void manageErrorResponse(Response response) {
    try {
      ErrorResponse errorResponse = objectMapper.readValue(
          response.readEntity(String.class), ErrorResponse.class);
      throw new CreditorInstitutionException(errorResponse,
          "[Payment Options] Encountered a managed error calling the station REST endpoint");
    } catch (CreditorInstitutionException creditorInstitutionException) {
      throw creditorInstitutionException;
    } catch (Exception e) {
      logger.error("[Payment Options] Unable to call the station due to error: {}",
          e.getMessage());
      throw new PaymentOptionsException(
          AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, e.getMessage());
    }
  }

}
