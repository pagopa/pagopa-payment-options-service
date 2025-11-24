package it.gov.pagopa.payment.options.clients;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import it.gov.pagopa.payment.options.services.CreditorInstitutionService;
import it.gov.pagopa.payment.options.test.extensions.WireMockExtensions;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class CreditorInstitutionRestClientTest {

  private static final String TARGET_HOST = "http://externalService";
  private static final long TARGET_PORT = 443L;

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
  private String wiremockUrl;

  @ConfigProperty(name = "wiremock.port")
  private String wiremockPort;

  @Inject CreditorInstitutionRestClient creditorInstitutionRestClient;
  
  @Inject CreditorInstitutionService sut;

  @Test
  void callEcPaymentOptionsVerifyShouldReturnData() {
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    new URL(wiremockUrl),
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/77777777777/notices/311111111112222222"));

    assertNotNull(paymentOptionsResponse);
  }

  @ParameterizedTest
  @ValueSource(strings = {"87777777777", "57777777777", "97777777777"})
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnErrorResponseWithFailureOnResponseParseOrValidation(
      String fiscalCode) {
    URL url = new URL(wiremockUrl);
    String targetPath =
        String.format("/payment-options/organizations/%s/notices/311111111112222222", fiscalCode);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url, null, null, TARGET_HOST, TARGET_PORT, targetPath));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode(),
        exception.getErrorResponse().getHttpStatusCode());
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
    assertEquals(
        CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
        exception.getErrorResponse().getErrorMessage());
  }

  @Test
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnErrorResponse() {
    URL url = new URL(wiremockUrl);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url,
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/67777777777/notices/311111111112222222"));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode(),
        exception.getErrorResponse().getHttpStatusCode());
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
    assertTrue(
        exception
            .getErrorResponse()
            .getErrorMessage()
            .startsWith(AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.name()));
  }

  @Test
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnUnreachableKOWithoutErrorResponse() {
    URL url = new URL(wiremockUrl);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url,
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/08888888888/notices/88888888888"));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnMalformedUrl() {
    assertThrows(
        MalformedURLException.class,
        () ->
            creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                new URL("AAAAAAA"),
                null,
                null,
                TARGET_HOST,
                TARGET_PORT,
                "/payment-options/organizations/08888888888/notices/88888888888"));
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnWrongProxy() {
    assertThrows(
        Exception.class,
        () ->
            creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                new URL("AAAAAAA"),
                "AAAAAAA%%%",
                8081L,
                TARGET_HOST,
                TARGET_PORT,
                "/payment-options/organizations/08888888888/notices/88888888888"));
  }
  
 //============================================================
 //  GPD-Core "special guest"
 // ============================================================

 @Test
 void callGpdPaymentOptionsVerifyShouldThrowSemanticaOnNullEndpoint() {
   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                   null, "77777777777", "311111111112222222", null));

   assertNotNull(ex);
   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
   assertTrue(ex.getMessage().contains("Malformed GPD-Core endpoint"));
 }

 @ParameterizedTest
 @ValueSource(strings = {"", "   ", "not-a-valid-url"})
 void callGpdPaymentOptionsVerifyShouldThrowSemanticaOnInvalidEndpoint(String gpdEndpoint) {
   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                   gpdEndpoint, "77777777777", "311111111112222222", null));

   assertNotNull(ex);
   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
 }

 @Test
 void callGpdPaymentOptionsVerifyShouldPropagatePaymentOptionsException() {
   PaymentOptionsException clientException =
       new PaymentOptionsException(
           AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
           "[Payment Options] Unable to reach GPD-Core endpoint");

   CreditorInstitutionRestClient spyClient =
       Mockito.spy(creditorInstitutionRestClient);

   Mockito.doThrow(clientException)
       .when(spyClient)
       .callGpdPaymentOptionsVerify(any(), any(), any(), any());

   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               spyClient.callGpdPaymentOptionsVerify(
                   "http://dummy-gpd-endpoint", "77777777777", "311111111112222222", null));

   assertSame(clientException, ex);
   assertSame(clientException.getErrorCode(), ex.getErrorCode());
 }
  
}