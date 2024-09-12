package it.gov.pagopa.payment.options.clients;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.WireMockExtensions;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.models.clients.cache.Connection;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class CreditorInstitutionRestClientTest {

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
  private String wiremockUrl;

  @Inject
  CreditorInstitutionRestClient creditorInstitutionRestClient;

  @Test
  void callEcPaymentOptionsVerifyShouldReturnData() {
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        wiremockUrl, null, null,
        "http://externalService", 443L, "/externalPath",
          "88888888888", "88888888888", "88888888888_01",
        "77777777777", "311111111112222222"));
    assertNotNull(paymentOptionsResponse);
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnErrorResponse() {
    CreditorInstitutionException exception =
        assertThrows(CreditorInstitutionException.class,
            () -> creditorInstitutionRestClient.callEcPaymentOptionsVerify(
            wiremockUrl, null, null,
            "http://externalService", 443L, "/externalPath",
            "88888888888", "88888888888", "88888888888_01",
            "87777777777", "311111111112222222"));
    assertNotNull(exception);
    assertEquals(exception.getErrorResponse().getHttpStatusCode(), 500);
  }


}
