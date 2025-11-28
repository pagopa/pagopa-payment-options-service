package it.gov.pagopa.payment.options.resources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.services.PaymentOptionsService;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaymentOptionsResourceTest {

  @InjectMock
  PaymentOptionsService paymentOptionsService;

  @Inject
  PaymentOptionsResource paymentOptionsResource;

  @BeforeEach
  public void init() {
    Mockito.reset(paymentOptionsService);
  }

  @Test
  void getPaymentOptionsShouldReturnOk() {
    when(paymentOptionsService.getPaymentOptions(any(), any(), any(), any(), any())).thenReturn(
        PaymentOptionsResponse.builder().build());
    RestResponse<PaymentOptionsResponse> paymentOptionsResponse = assertDoesNotThrow(
        () -> paymentOptionsResource.getPaymentOptions(
        "00001", "300000001", "00001", "00001", null));
    assertNotNull(paymentOptionsResponse);
    assertNotNull(paymentOptionsResponse.getEntity());
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnCreditorInstitutionException() {
    when(paymentOptionsService.getPaymentOptions(any(), any(), any(), any(), any())).thenThrow(
        new CreditorInstitutionException(ErrorResponse.builder().build(), "message"));
    CreditorInstitutionException creditorInstitutionException = assertThrows(
        CreditorInstitutionException.class, () -> paymentOptionsResource.getPaymentOptions(
        "00001", "300000001", "00001", "00001", null));
    assertNotNull(creditorInstitutionException);
  }

  @Test
  void getPaymentOptionsShouldReturnExceptionOnPaymentOptionsException() {
    when(paymentOptionsService.getPaymentOptions(any(), any(), any(), any(), any())).thenThrow(
        new PaymentOptionsException(AppErrorCodeEnum.ODP_SYSTEM_ERROR, "message"));
    PaymentOptionsException paymentOptionsException = assertThrows(
        PaymentOptionsException.class, () -> paymentOptionsResource.getPaymentOptions(
            "00001", "300000001", "00001", "00001", null));
    assertNotNull(paymentOptionsException);
  }

}
