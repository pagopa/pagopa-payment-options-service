package it.gov.pagopa.payment.options.clients;

import static it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Rest Client for Creditor Institution services */
@ApplicationScoped
public class CreditorInstitutionRestClient {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionRestClient.class);

  @Inject ObjectMapper objectMapper;

  /**
   * Call the creditor institution service to obtain the list paymentOptions related to the input
   *
   * @param proxyHost proxy host, optional
   * @param proxyPort proxy port, optional
   * @param targetHost verify service host
   * @param targetPort verify service port
   * @param targetPath verify service path
   * @return PaymentOptionResponse
   * @throws CreditorInstitutionException when an error occurred while processing creditor
   *     institution response
   * @throws PaymentOptionsException when an unexpected error occurred
   */
  public PaymentOptionsResponse callEcPaymentOptionsVerify(
      URL baseUrl,
      String proxyHost,
      Long proxyPort,
      String targetHost,
      Long targetPort,
      String targetPath) {

    RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(baseUrl);

    if (proxyHost != null && proxyPort != null) {
      builder = builder.proxyAddress(proxyHost, proxyPort.intValue());
    }
    CreditorInstitutionRestClientInterface ecRestClientInterface =
        builder.build(CreditorInstitutionRestClientInterface.class);

    try {
      return getPaymentOptions(targetHost, targetPort, targetPath, ecRestClientInterface);
    } catch (CreditorInstitutionException e) {
      throw e;
    } catch (JsonProcessingException e) {
      LocalDateTime now = LocalDateTime.now();
      ErrorResponse errorResponse =
          buildErrorResponse(
              CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
              now.getLong(ChronoField.MILLI_OF_SECOND),
              now.toString());
      throw new CreditorInstitutionException(
          errorResponse, "[Payment Options] Unable to parse the station response");
    } catch (Exception e) {
      logger.error("[Payment Options] Unable to call the station due to error", e);
      throw new PaymentOptionsException(
          AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, e.getMessage());
    }
  }

  private PaymentOptionsResponse getPaymentOptions(
      String targetHost,
      Long targetPort,
      String targetPath,
      CreditorInstitutionRestClientInterface ecRestClientInterface)
      throws JsonProcessingException {
    try (Response response =
        ecRestClientInterface.verifyPaymentOptions(targetHost, targetPort.intValue(), targetPath)) {

      return this.objectMapper.readValue(
          response.readEntity(String.class), PaymentOptionsResponse.class);

    } catch (ClientWebApplicationException e) {
      logger.error("[Payment Options] Encountered REST client exception", e);
      Response response = e.getResponse();
      ErrorResponse errorResponse =
          this.objectMapper.readValue(response.readEntity(String.class), ErrorResponse.class);
      errorResponse = validateAndBuildErrorResponse(response.getStatus(), errorResponse);

      throw new CreditorInstitutionException(
          errorResponse,
          "[Payment Options] Encountered a managed error calling the station REST endpoint");
    }
  }

  private ErrorResponse validateAndBuildErrorResponse(
      int responseStatus, ErrorResponse errorResponse) {
    String responseErrorCode = errorResponse.getAppErrorCode();
    String errorMessage =
        String.format(
            "%s %s %s",
            ODP_ERRORE_EMESSO_DA_PAA.name(), responseErrorCode, errorResponse.getErrorMessage());
    ErrorResponse errorResponseForPSP =
        buildErrorResponse(errorMessage, errorResponse.getTimestamp(), errorResponse.getDateTime());

    if (CreditorInstitutionErrorEnum.isNotValidErrorCode(responseErrorCode)
        || CreditorInstitutionErrorEnum.getFromErrorCode(responseErrorCode).getStatus()
            != responseStatus) {
      errorResponseForPSP.setErrorMessage(CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name());
    }
    return errorResponseForPSP;
  }

  private ErrorResponse buildErrorResponse(String errorMessage, Long timestamp, String dateTime) {
    return ErrorResponse.builder()
        .httpStatusCode(ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode())
        .httpStatusDescription(ODP_ERRORE_EMESSO_DA_PAA.getStatus().getReasonPhrase())
        .appErrorCode(ODP_ERRORE_EMESSO_DA_PAA.getErrorCode())
        .errorMessage(errorMessage)
        .timestamp(timestamp)
        .dateTime(dateTime)
        .build();
  }
}
