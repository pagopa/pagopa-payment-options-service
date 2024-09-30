package it.gov.pagopa.payment.options.clients;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.test.extensions.WireMockExtensions;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class CreditorInstitutionRestClientTest {

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
  private String wiremockUrl;

  @ConfigProperty(name = "wiremock.port")
  private String wiremockPort;


  @Inject
  CreditorInstitutionRestClient creditorInstitutionRestClient;

  @Test
  void callEcPaymentOptionsVerifyShouldReturnData() {
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        wiremockUrl, null, null,
        "http://externalService", 443L, "/payment-options/organizations/77777777777/notices/311111111112222222",
          "77777777777", "311111111112222222"));
    assertNotNull(paymentOptionsResponse);
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnErrorResponse() {
    CreditorInstitutionException exception =
        assertThrows(CreditorInstitutionException.class,
            () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
            wiremockUrl, null, null,
            "http://externalService", 443L, "/payment-options/organizations/87777777777/notices/311111111112222222",
            "87777777777", "311111111112222222"));
    assertNotNull(exception);
    assertEquals(exception.getErrorResponse().getHttpStatusCode(), 500);
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnErrorOnUnexpectedContent() {
    PaymentOptionsException exception =
        assertThrows(PaymentOptionsException.class,
            () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                wiremockUrl, null, null,
                "http://externalService", 443L, "/payment-options/organizations/97777777777/notices/311111111112222222",
                "97777777777", "311111111112222222"));
    assertNotNull(exception);
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnUnreachableKOWithoutErrorResponse() {
    PaymentOptionsException exception =
        assertThrows(PaymentOptionsException.class,
            () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                wiremockUrl, null, null,
                "http://externalService", 443L, "/payment-options/organizations/08888888888/notices/88888888888",
                "08888888888", "88888888888"));
    assertNotNull(exception);
    assertEquals(exception.getErrorCode(), AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE);
  }


  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnMalformedUrl() {
        assertThrows(MalformedURLException.class,
            () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                "AAAAAAA", null, null,
                "http://externalService", 443L, "/payment-options/organizations/08888888888/notices/88888888888",
                "88888888888", "88888888888"));
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnWrongProxy() {
    assertThrows(Exception.class,
        () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
            "AAAAAAA", "AAAAAAA%%%", 8081L,
            "http://externalService", 443L, "/payment-options/organizations/08888888888/notices/88888888888",
            "88888888888", "88888888888"));
  }




}
