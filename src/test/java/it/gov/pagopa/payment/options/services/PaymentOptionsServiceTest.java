package it.gov.pagopa.payment.options.services;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerPsp;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.CreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.PaymentServiceProvider;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.cache.StationCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaymentOptionsServiceTest {

  @InjectMock
  public CreditorInstitutionService creditorInstitutionService;

  @InjectMock
  public ConfigCacheService configCacheService;

  @Inject
  public PaymentOptionsService paymentOptionsService;

  @BeforeEach
  public void init() {
    Mockito.reset(configCacheService, creditorInstitutionService);
  }

  @Test
  void getPaymentOptionsShouldReturnOkOnValidDataAndResponse() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
            .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
            .stations(Map.of("00001",
                Station.builder().enabled(true).verifyPaymentOptionEnabled(true).build()))
            .creditorInstitutions(Map.of("00001",
                CreditorInstitution.builder()
                    .creditorInstitutionCode("00001").enabled(true).build()))
            .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
            .creditorInstitutionStations(Map.of("00001",
                StationCreditorInstitution.builder()
                    .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L)
                    .build())
            )
            .creditorInstitutionBrokers(
                Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
            )
        .build());

    when(creditorInstitutionService.getPaymentOptions(any(), any(), any())).thenReturn(
        PaymentOptionsResponse.builder().build());

    PaymentOptionsResponse paymentOptionsResponse = paymentOptionsService.getPaymentOptions(
        "00001", "00001", "00001", "3000000000");
    assertNotNull(paymentOptionsResponse);
    verify(configCacheService).getConfigCacheData();
    verify(creditorInstitutionService).getPaymentOptions(any(), any(), any());

  }

}
