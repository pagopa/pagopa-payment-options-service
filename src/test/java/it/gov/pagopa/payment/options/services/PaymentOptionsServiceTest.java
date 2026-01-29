package it.gov.pagopa.payment.options.services;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerPsp;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.CreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.PaymentServiceProvider;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.cache.StationCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaymentOptionsServiceTest {

  @InjectMock
  public CreditorInstitutionService creditorInstitutionService;

  @InjectMock
  public ConfigCacheService configCacheService;

  @InjectMock
  EventService eventService;

  @Inject
  public PaymentOptionsService paymentOptionsService;

  @BeforeEach
  void init() {
	  Mockito.reset(configCacheService, creditorInstitutionService, eventService);

	  // Default stub for the new station resolution path:
	  // PaymentOptionsService now calls ConfigCacheService.resolveStationCode(...) instead of
	  // scanning creditorInstitutionStations from ConfigDataV1. Since ConfigCacheService is mocked,
	  // we must stub this method to drive the expected results in tests.
	  when(configCacheService.resolveStationCode(any(), anyLong())).thenReturn("00001");
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
                    .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                    .build())
            )
            .creditorInstitutionBrokers(
                Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
            )
        .build());

    when(creditorInstitutionService.getPaymentOptions(any(), any(), any(), anyLong())).thenReturn(
        PaymentOptionsResponse.builder().build());

    PaymentOptionsResponse paymentOptionsResponse = assertDoesNotThrow(() ->
        paymentOptionsService.getPaymentOptions(
        "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsResponse);
    verify(configCacheService).getConfigCacheData();
    verify(creditorInstitutionService).getPaymentOptions(any(), any(), any(), anyLong());

  }

  @Test
  void getPaymentOptionsShouldReturnKoOnApiExceptionAndResponse() {

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
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    when(creditorInstitutionService.getPaymentOptions(any(), any(), any(), anyLong())).thenThrow(
        new CreditorInstitutionException(
            ErrorResponse.builder().appErrorCode("ODP-001")
                .errorMessage("test").build(), "test"));

    CreditorInstitutionException creditorInstitutionException = assertThrows(
        CreditorInstitutionException.class, () -> paymentOptionsService.getPaymentOptions(
        "00001", "00001", "00001", "3000000000", null));
    assertNotNull(creditorInstitutionException);
    verify(configCacheService).getConfigCacheData();
    verify(creditorInstitutionService).getPaymentOptions(any(), any(), any(), anyLong());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnOdpDisabled() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .stations(Map.of("00001",
            Station.builder().enabled(true).verifyPaymentOptionEnabled(false).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionStations(Map.of("00001",
            StationCreditorInstitution.builder()
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_VERIFICA_ODP_DISABILITATA, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnStationDisabled() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .stations(Map.of("00001",
            Station.builder().enabled(false).verifyPaymentOptionEnabled(false).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionStations(Map.of("00001",
            StationCreditorInstitution.builder()
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_DISABILITATA, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnStationUnknown() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .stations(Map.of("00003",
            Station.builder().enabled(false).verifyPaymentOptionEnabled(false).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionStations(Map.of("00001",
            StationCreditorInstitution.builder()
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnStationDataMissing() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionStations(Map.of("00001",
            StationCreditorInstitution.builder()
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(3L).segregationCode(0L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_SYSTEM_ERROR, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnStationExtractedUnknown() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        // The service checks that stations data is available before attempting station resolution.
        .stations(Map.of("00001",
                Station.builder().enabled(true).verifyPaymentOptionEnabled(true).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionStations(Map.of("00001",
            StationCreditorInstitution.builder()
                .creditorInstitutionCode("00001").stationCode("00001").auxDigit(0L).segregationCode(1L)
                .build())
        )
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());
    
    when(configCacheService.resolveStationCode("00001", 0L)).thenReturn(null);

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_SCONOSCIUTA, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnCreditorInstitutionStationDataMissing() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(true).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());
    
    when(configCacheService.resolveStationCode(any(), anyLong()))
    .thenThrow(new PaymentOptionsException(
        AppErrorCodeEnum.ODP_SYSTEM_ERROR,
        "Configuration data currently not available"
    ));

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_SYSTEM_ERROR, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnCreditorInstitutionDisabled() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        // The service checks that stations data is available before attempting station resolution.
        .stations(Map.of("00001",
                Station.builder().enabled(true).verifyPaymentOptionEnabled(true).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(false).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_DOMINIO_DISABILITATO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnCreditorInstitutionUnknown() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        // The service requires the stations map to be present before performing downstream validations.
        .stations(Map.of(
        	    "00001", Station.builder().enabled(true).verifyPaymentOptionEnabled(true).build()
        ))
        .creditorInstitutions(Map.of("30001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("30001").enabled(false).build()))
        .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_DOMINIO_SCONOSCIUTO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnPspBrokerDisabled() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(false).build()))
        .pspBrokers(Map.of("30001", BrokerPsp.builder().enabled(false).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_SCONOSCIUTO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnPspBrokerUnknown() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(true).build()))
        .creditorInstitutions(Map.of("30001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("30001").enabled(false).build()))
        .pspBrokers(Map.of("30001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_INTERMEDIARIO_PSP_SCONOSCIUTO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnPspDisabled() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("00001", PaymentServiceProvider.builder().enabled(false).build()))
        .creditorInstitutions(Map.of("00001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("00001").enabled(false).build()))
        .pspBrokers(Map.of("30001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_PSP_DISABILITATO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnPspUnknown() {

    when(configCacheService.getConfigCacheData()).thenReturn(ConfigDataV1
        .builder()
        .psps(Map.of("30001", PaymentServiceProvider.builder().enabled(true).build()))
        .creditorInstitutions(Map.of("30001",
            CreditorInstitution.builder()
                .creditorInstitutionCode("30001").enabled(false).build()))
        .pspBrokers(Map.of("30001", BrokerPsp.builder().enabled(true).build()))
        .creditorInstitutionBrokers(
            Map.of("00001", BrokerCreditorInstitution.builder().enabled(true).build())
        )
        .build());

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_PSP_SCONOSCIUTO, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnInvalidNav() {

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "0000000000", null));
    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_PSP_NAV_NOT_NMU, paymentOptionsException.getErrorCode());
    verify(eventService).sendVerifyKoEvent(
        any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnMissingIdPsp() {

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            null, "00001", "00001", "0000000000", null));
    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SINTASSI, paymentOptionsException.getErrorCode());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnMissingIdBrokerPsp() {

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", null, "00001", "0000000000", null));
    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SINTASSI, paymentOptionsException.getErrorCode());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnMissingFiscalCode() {

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", null, "0000000000", null));
    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SINTASSI, paymentOptionsException.getErrorCode());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnMissingNoticeNumber() {

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", null, null));
    assertNotNull(paymentOptionsException);
    assertEquals(AppErrorCodeEnum.ODP_SINTASSI, paymentOptionsException.getErrorCode());
  }

  @Test
  void getPaymentOptionsShouldReturnKoOnCacheConfigError() {

    when(configCacheService.getConfigCacheData()).thenThrow(new RuntimeException("Error"));

    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsService.getPaymentOptions(
            "00001", "00001", "00001", "3000000000", null));
    assertNotNull(paymentOptionsException);
    verify(configCacheService).getConfigCacheData();
    assertEquals(AppErrorCodeEnum.ODP_SYSTEM_ERROR, paymentOptionsException.getErrorCode());
  }

}
