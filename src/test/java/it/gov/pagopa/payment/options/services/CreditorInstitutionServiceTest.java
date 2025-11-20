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
        any(), any(), any(), any(), any(), any(), any(), any())
    ).thenReturn(PaymentOptionsResponse.builder().build());
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> creditorInstitutionService.getPaymentOptions(
        "000001","000001",
        Station.builder().stationCode("000001_01")
            .connection(
                Connection.builder()
                    .ip("localhost")
                    .protocol(ProtocolEnum.HTTP)
                    .port(8082L)
                    .build()
            )
            .restEndpoint("http://localhost:8080/test")
            .verifyPaymentOptionEnabled(true)
            .build()
    ));
    assertNotNull(paymentOptionsResponse);
    verify(creditorInstitutionRestClient).callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnDataWithDefaultPort() throws MalformedURLException {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any())
    ).thenReturn(PaymentOptionsResponse.builder().build());
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(() -> creditorInstitutionService.getPaymentOptions(
            "000001","000001",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .restEndpoint("http://localhost/test")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsResponse);
    verify(creditorInstitutionRestClient).callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMalformed() throws MalformedURLException {
    when(creditorInstitutionRestClient.callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any())
    ).thenThrow(new MalformedURLException());
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .restEndpoint("http://localhost:8080/test")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_SEMANTICA);
    verify(creditorInstitutionRestClient).callEcPaymentOptionsVerify(
        any(), any(), any(), any(), any(), any(), any(), any()
    );
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMissingEndpoint() throws MalformedURLException {
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .restEndpoint(null)
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
            "000001","000001",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .ip("localhost")
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .restEndpoint(":8080")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_SEMANTICA);
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnMissingConnection() throws MalformedURLException {
    PaymentOptionsException paymentOptionsException =
        assertThrows(PaymentOptionsException.class, () -> creditorInstitutionService.getPaymentOptions(
            "000001","000001",
            Station.builder().stationCode("000001_01")
                .connection(
                    Connection.builder()
                        .protocol(ProtocolEnum.HTTP)
                        .port(8082L)
                        .build()
                )
                .restEndpoint(":8080")
                .verifyPaymentOptionEnabled(true)
                .build()
        ));
    assertNotNull(paymentOptionsException);
    assertEquals(paymentOptionsException.getErrorCode(), AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE);
  }
  
  
  // ----------------- GPD Special Guest Tests -----------------
  @Test
  void getPaymentOptionsGpdSpecialGuestRestEndpointMatches()
      throws MalformedURLException {

    String gpdEndpoint = "http://localhost:8080";

    when(creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
        any(), any(), any(), any()))
        .thenReturn(PaymentOptionsResponse.builder().build());

    PaymentOptionsResponse response = assertDoesNotThrow(
        () -> creditorInstitutionService.getPaymentOptions(
            "000001", "000001",
            Station.builder()
                .stationCode("000001_01")
                .connection(
                    it.gov.pagopa.payment.options.models.clients.cache.Connection.builder()
                        .ip("some-ip")
                        .protocol(ProtocolEnum.HTTPS)
                        .port(443L)
                        .build()
                )
                .restEndpoint(gpdEndpoint)
                .verifyPaymentOptionEnabled(true)
                .build()
        )
    );

    assertNotNull(response);

    // ONLY the GPD client should be called
    verify(creditorInstitutionRestClient, Mockito.times(1))
    .callGpdPaymentOptionsVerify(any(), any(), any(), any());

    verify(creditorInstitutionRestClient, Mockito.never())
    .callEcPaymentOptionsVerify(any(), any(), any(), any(), any(), any(), any(), any());

  }
  
  @Test
  void getPaymentOptionsMalformedUrlGpdToPaymentOptionsException()
      throws MalformedURLException {

    String gpdEndpoint = "http://localhost";

    when(creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
        any(), any(), any(), any()))
        .thenThrow(new MalformedURLException("bad gpd url"));

    PaymentOptionsException ex = assertThrows(
        PaymentOptionsException.class,
        () -> creditorInstitutionService.getPaymentOptions(
            "000001", "000001",
            Station.builder()
                .stationCode("000001_01")
                .connection(
                    it.gov.pagopa.payment.options.models.clients.cache.Connection.builder()
                        .ip("some-ip")
                        .protocol(ProtocolEnum.HTTPS)
                        .port(443L)
                        .build()
                )
                .restEndpoint(gpdEndpoint)
                .verifyPaymentOptionEnabled(true)
                .build()
        )
    );

    assertNotNull(ex);
    assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, ex.getErrorCode());
  }
  
  @Test
  void getPaymentOptionsGpdSpecialGuestPropagatesPaymentOptionsException()
      throws MalformedURLException {

    String gpdEndpoint = "http://localhost:8080";

    PaymentOptionsException clientException = new PaymentOptionsException(
        AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
        "Unable to reach GPD-Core endpoint"
    );

    when(creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
        any(), any(), any(), any()))
        .thenThrow(clientException);

    PaymentOptionsException ex = assertThrows(
        PaymentOptionsException.class,
        () -> creditorInstitutionService.getPaymentOptions(
            "000001", "000001",
            Station.builder()
                .stationCode("000001_01")
                .connection(
                    it.gov.pagopa.payment.options.models.clients.cache.Connection.builder()
                        .ip("some-ip")
                        .protocol(ProtocolEnum.HTTPS)
                        .port(443L)
                        .build()
                )
                .restEndpoint(gpdEndpoint)
                .verifyPaymentOptionEnabled(true)
                .build()
        )
    );

    assertSame(clientException.getErrorCode(), ex.getErrorCode());
  }

}
