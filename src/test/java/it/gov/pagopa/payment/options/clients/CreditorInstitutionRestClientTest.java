package it.gov.pagopa.payment.options.clients;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import it.gov.pagopa.payment.options.services.CreditorInstitutionService;
import it.gov.pagopa.payment.options.test.extensions.WireMockExtensions;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.net.MalformedURLException;
import java.net.URL;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

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
  void callGpdPaymentOptionsVerifyShouldReturnDataOn2xx() throws Exception {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      PaymentOptionsResponse expected = PaymentOptionsResponse.builder().build();
      String body = om.writeValueAsString(expected);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(200);
      when(response.readEntity(String.class)).thenReturn(body);

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenReturn(response);

      PaymentOptionsResponse result =
          client.callGpdPaymentOptionsVerify("77777777777", "311111111111111111", null);

      assertNotNull(result);
  }
  
 
  @Test
  void callGpdPaymentOptionsVerifyShouldMapBusinessErrorFromGpd() throws Exception {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      ErrorResponse err = ErrorResponse.builder()
          .httpStatusCode(404)
          .httpStatusDescription("Not Found")
          .appErrorCode("ODP-404")
          .timestamp(1724425035L)
          .dateTime("2024-08-23T14:57:15.635528")
          .errorMessage("PAA_SOME_ERROR details")
          .build();

      String json = om.writeValueAsString(err);

      Response resp = mock(Response.class);
      when(resp.getStatus()).thenReturn(404);
      when(resp.readEntity(String.class)).thenReturn(json);

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenReturn(resp);

      CreditorInstitutionException ex = assertThrows(
          CreditorInstitutionException.class,
          () -> client.callGpdPaymentOptionsVerify(
              "77777777777", "311111111111111111", null)
      );

      assertNotNull(ex.getErrorResponse());
      assertEquals(
    		  ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode(),
    		  ex.getErrorResponse().getHttpStatusCode()
    		  );
      assertEquals(
    		  ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
    		  ex.getErrorResponse().getAppErrorCode()
    		  );
      assertEquals(
    		  CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
    		  ex.getErrorResponse().getErrorMessage()
    		  );
  }

 
  @Test
  void callGpdPaymentOptionsVerifyShouldMapNetworkErrorToPaymentOptionsException() {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      ClientWebApplicationException cwae = mock(ClientWebApplicationException.class);
      when(cwae.getResponse()).thenReturn(null);

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenThrow(cwae);

      PaymentOptionsException ex = assertThrows(
          PaymentOptionsException.class,
          () -> client.callGpdPaymentOptionsVerify(
              "77777777777", "311111111111111111", null)
      );

      assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, ex.getErrorCode());
  }

 
  @Test
  void callGpdPaymentOptionsVerifyShouldMapParsingErrorToCreditorInstitutionException() {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(200);
      when(response.readEntity(String.class)).thenReturn("NOT_JSON");

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenReturn(response);

      CreditorInstitutionException ex = assertThrows(
          CreditorInstitutionException.class,
          () -> client.callGpdPaymentOptionsVerify(
              "77777777777", "311111111111111111", null)
      );

      assertEquals(
          CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
          ex.getErrorResponse().getErrorMessage()
      );
  }
  
  @Test
  void callGpdPaymentOptionsVerifyShouldThrowMappedCreditorInstitutionException() throws Exception {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      ErrorResponse err = ErrorResponse.builder()
          .httpStatusCode(500)
          .httpStatusDescription("Error")
          .appErrorCode("PAA_SYSTEM_ERROR")
          .timestamp(1L)
          .dateTime("2024-01-01T00:00:00")
          .errorMessage("details")
          .build();

      String json = om.writeValueAsString(err);

      Response resp = mock(Response.class);
      when(resp.getStatus()).thenReturn(500);
      when(resp.readEntity(String.class)).thenReturn(json);

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenReturn(resp);

      RuntimeException ex = assertThrows(
          RuntimeException.class,
          () -> client.callGpdPaymentOptionsVerify("X","Y","Z")
      );

      assertTrue(ex instanceof CreditorInstitutionException);
  } 
  
  @Test
  void callGpdPaymentOptionsVerifyShouldMapInvalidBusinessErrorJsonToPaymentOptionsException() throws Exception {

      GpdCoreRestClientInterface gpdMock = mock(GpdCoreRestClientInterface.class);
      ObjectMapper om = new ObjectMapper();

      CreditorInstitutionRestClient client =
          new CreditorInstitutionRestClient(om, gpdMock);

      Response resp = mock(Response.class);
      when(resp.readEntity(String.class)).thenReturn("THIS_IS_NOT_JSON");
      when(resp.getStatus()).thenReturn(400);  // 4xx → business error

      ClientWebApplicationException cwae = mock(ClientWebApplicationException.class);
      when(cwae.getResponse()).thenReturn(resp);

      when(gpdMock.verifyPaymentOptions(any(), any(), any()))
          .thenThrow(cwae);

      PaymentOptionsException ex = assertThrows(
          PaymentOptionsException.class,
          () -> client.callGpdPaymentOptionsVerify(
              "77777777777", "311111111111111111", null
          )
      );

      assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
  }

  
}