package it.gov.pagopa.payment.options.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

@QuarkusTest
class CreditorInstitutionServiceTest {

  private static final String NOTICE_NUMBER = "000001";
  private static final String FISCAL_CODE = "000002";

  @InjectMock CreditorInstitutionRestClient creditorInstitutionRestClient;

  @Inject CreditorInstitutionService sut;

  @BeforeEach
  void init() {
    Mockito.reset(creditorInstitutionRestClient);
  }

  @Test
  void getPaymentOptionsShouldReturnData() {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
            any(), any(), any(), any(), any(), any()))
        .thenReturn(PaymentOptionsResponse.builder().build());

    Station station = buildStation("localhost", "http://localhost:8080/test");

    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> sut.getPaymentOptions(NOTICE_NUMBER, FISCAL_CODE, station));

    assertNotNull(paymentOptionsResponse);
    verify(creditorInstitutionRestClient)
        .callEcPaymentOptionsVerify(any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnDataWithDefaultPort() {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
            any(), any(), any(), any(), any(), any()))
        .thenReturn(PaymentOptionsResponse.builder().build());

    Station station = buildStation("localhost", "http://localhost/test");

    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> sut.getPaymentOptions(NOTICE_NUMBER, FISCAL_CODE, station));

    assertNotNull(paymentOptionsResponse);
    verify(creditorInstitutionRestClient)
        .callEcPaymentOptionsVerify(any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMissingEndpoint() {
    Station station = buildStation("localhost", null);

    PaymentOptionsException paymentOptionsException =
        assertThrows(
            PaymentOptionsException.class,
            () -> sut.getPaymentOptions(NOTICE_NUMBER, FISCAL_CODE, station));

    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, paymentOptionsException.getErrorCode());
    verifyNoInteractions(creditorInstitutionRestClient);
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnBrokerServiceUrl() {
    Station station = buildStation("localhost", ":8080");

    PaymentOptionsException paymentOptionsException =
        assertThrows(
            PaymentOptionsException.class,
            () -> sut.getPaymentOptions(NOTICE_NUMBER, FISCAL_CODE, station));

    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, paymentOptionsException.getErrorCode());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMissingConnection() {
    Station station = buildStation(null, ":8080");

    PaymentOptionsException paymentOptionsException =
        assertThrows(
            PaymentOptionsException.class,
            () -> sut.getPaymentOptions(NOTICE_NUMBER, FISCAL_CODE, station));

    assertNotNull(paymentOptionsException);
    assertEquals(
        AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
        paymentOptionsException.getErrorCode());
  }

  private Station buildStation(String connectionIp, String restEndpoint) {
    return Station.builder()
        .stationCode("000001_01")
        .connection(
            Connection.builder().ip(connectionIp).protocol(ProtocolEnum.HTTP).port(8082L).build())
        .restEndpoint(restEndpoint)
        .verifyPaymentOptionEnabled(true)
        .build();
  }
}
