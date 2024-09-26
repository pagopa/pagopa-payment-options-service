package it.gov.pagopa.payment.options;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResource.List;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import it.gov.pagopa.payment.options.services.ConfigCacheService;
import it.gov.pagopa.payment.options.services.CreditorInstitutionService;
import it.gov.pagopa.payment.options.services.EventService;
import it.gov.pagopa.payment.options.services.PaymentOptionsService;
import it.gov.pagopa.payment.options.test.extensions.KafkaTestResourceLifecycleManager;
import it.gov.pagopa.payment.options.test.extensions.WireMockExtensions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@List({
    @QuarkusTestResource(value = WireMockExtensions.class),
    @QuarkusTestResource(value = KafkaTestResourceLifecycleManager.class)
})
public class PaymentOptionsExtendedTest {

  @InjectSpy
  private PaymentOptionsService paymentOptionsService;

  @InjectSpy
  ConfigCacheService configCacheService;

  @InjectSpy
  CreditorInstitutionService creditorInstitutionService;

  @InjectSpy
  EventService eventService;

  @Test
  public void verifyOptionRequestOnValidDataShouldReturnOkResponse() {

    given()
        .when().get("/payment-options/organizations/77777777777/notices/311111111112222222?idPsp=00001&idBrokerPsp=00001")
        .then()
        .statusCode(200);
    verify(configCacheService).getConfigCacheData();
    verify(paymentOptionsService).getPaymentOptions(
        eq("00001"), eq("00001"),
        eq("77777777777"),eq("311111111112222222"), any());
    verify(creditorInstitutionService).getPaymentOptions(
        eq("311111111112222222"), eq("77777777777"), any());
    verify(eventService, times(0)).sendVerifyKoEvent(
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

  }

}
