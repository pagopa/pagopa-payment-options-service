package it.gov.pagopa.payment.options.services;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.clients.CreditorInstitutionRestClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.Connection;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class CreditorInstitutionServiceTest {

  @InjectMock
  CreditorInstitutionRestClient creditorInstitutionRestClient;

  @Inject
  CreditorInstitutionService creditorInstitutionService;

  @BeforeEach
  public void init() {
    Mockito.reset(creditorInstitutionRestClient);
  }

  @Test
  void getPaymentOptionsShouldReturnData() throws MalformedURLException {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
    ).thenReturn(PaymentOptionsResponse.builder().build());
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> creditorInstitutionService.getPaymentOptions(
        "000001","000001","12345","322334",
        Station.builder().stationCode("000001_01")
            .connection(
                Connection.builder()
                    .ip("localhost")
                    .protocol(ProtocolEnum.HTTP)
                    .port(8082L)
                    .build()
            )
            .verifyPaymentOptionEndpoint("http://localhost:8080/test")
            .verifyPaymentOptionEnabled(true)
            .build()
    ));
    assertNotNull(paymentOptionsResponse);
    verify(creditorInstitutionRestClient).callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMalformed() throws MalformedURLException {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
    ).thenThrow(new MalformedURLException());
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001","12345","322334",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .verifyPaymentOptionEndpoint("http://localhost:8080/test")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_SEMANTICA);
    verify(creditorInstitutionRestClient).callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMissingEndpoint() throws MalformedURLException {
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001","12345","322334",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .verifyPaymentOptionEndpoint(null)
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_SEMANTICA);
    verifyNoInteractions(creditorInstitutionRestClient);
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnBrokerServiceUrl() throws MalformedURLException {
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001","12345","322334",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .verifyPaymentOptionEndpoint(":8080")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_SEMANTICA);
  }


}
