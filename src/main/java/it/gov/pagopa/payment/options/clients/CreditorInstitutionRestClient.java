package it.gov.pagopa.payment.options.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.events.odpRe.Properties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Rest Client for Creditor Institution services
 */
@ApplicationScoped
public class CreditorInstitutionRestClient {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionRestClient.class);

  @Inject
  ObjectMapper objectMapper;

  /**
   *
   * @param endpoint endpoint to use for the call (should be equivalent to the forwader(
   * @param proxyHost proxy host, optional
   * @param proxyPort proxy port, optional
   * @param targetHost verify service host
   * @param targetPort verify service port
   * @param targetPath verify service path
   * @param fiscalCode fiscal code to be used as input for the call
   * @param noticeNumber notice number to be used as input for the call
   * @return PaymentOptionResponse
   * @throws MalformedURLException
   * @throws PaymentOptionsException
   */
  public PaymentOptionsResponse callEcPaymentOptionsVerify(
      String endpoint, String proxyHost, Long proxyPort,
      String targetHost, Long targetPort, String targetPath,
      String fiscalCode, String noticeNumber)
      throws MalformedURLException {

    RestClientBuilder builder =
        RestClientBuilder.newBuilder().baseUrl(
            new URL(String.format(endpoint, fiscalCode, noticeNumber)));

    if (proxyHost != null && proxyPort != null) {
      builder = builder.proxyAddress(proxyHost, proxyPort.intValue());
    }
    CreditorInstitutionRestClientInterface ecRestClientInterface = builder.build(
        CreditorInstitutionRestClientInterface.class);

    try (Response response = ecRestClientInterface.verifyPaymentOptions(
        targetHost, targetPort.intValue(), targetPath)) {

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
      logger.error(e.getMessage(), e);
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
