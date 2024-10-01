package it.gov.pagopa.payment.options.resources;

import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.BrokerPsp;
import it.gov.pagopa.payment.options.models.clients.cache.ConfigDataV1;
import it.gov.pagopa.payment.options.models.clients.cache.Connection;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.CreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.cache.PaymentServiceProvider;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.cache.StationCreditorInstitution;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.Installment;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOption;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.EnumInstallment;
import it.gov.pagopa.payment.options.models.enums.EnumPo;
import it.gov.pagopa.payment.options.services.PaymentOptionsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.RestResponse;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Exposes mocked REST interfaces for the payment options services
 */
@Path("/mock")
@Produces(value = MediaType.APPLICATION_JSON)
public class MockedResource {


  @GET
  @Path("/payment-options/organizations/{fiscal-code}/notices/{notice-number}")
  public RestResponse<PaymentOptionsResponse> getPaymentOptions(
      @PathParam("fiscal-code") String fiscalCode,
      @PathParam("notice-number") String noticeNumber
  ) {
    if (fiscalCode.equals("77777777777") && noticeNumber.equals("311111111111111111")) {
      return RestResponse.status(Status.OK,
          PaymentOptionsResponse.builder()
              .paFullName("EC")
              .paOfficeName("EC")
              .paTaxCode("99999000013")
              .standin(true)
              .paymentOptions(Collections.singletonList(
                  PaymentOption.builder()
                      .allCCP(true)
                      .amount(120L)
                      .description("Test Opt Inst - unica opzione")
                      .dueDate("2024-10-30T23:59:59")
                      .validFrom("2024-10-30T23:59:59")
                      .status(EnumPo.PO_UNPAID)
                      .numberOfInstallments(1)
                      .statusReason("Unpaid")
                      .installments(Collections.singletonList(
                          Installment.builder()
                              .amount(120L)
                              .description("Test Opt Inst - unica opzione")
                              .iuv("347000000880099993")
                              .nav("47000000880099993")
                              .validFrom("2024-10-30T23:59:59")
                              .dueDate("2024-10-30T23:59:59")
                              .status(EnumInstallment.POI_UNPAID)
                              .statusReason("Description")
                              .build()
                      ))
                      .build()
              ))
              .build());
    } else {
      throw new CreditorInstitutionException(ErrorResponse.builder()
          .httpStatusCode(500)
          .httpStatusDescription("Error")
          .appErrorCode("ODB_ERRID")
          .errorMessage("TEST")
          .build(), "Mock Error");
    }

  }


  @GET
  @Path("/cache")
  public ConfigDataV1 getCache() {
    return ConfigDataV1
        .builder()
        .psps(Map.of("00001",
            PaymentServiceProvider.builder().enabled(true).build()))
        .stations(Map.of("00001",
                Station.builder().stationCode("00001").enabled(true).verifyPaymentOptionEnabled(true)
                    .connection(Connection.builder()
                        .protocol(ProtocolEnum.HTTP).ip("localhost")
                        .port(8080L)
                        .build())
                    .restEndpoint("https://localhost:9095/test")
                .build()))
            .creditorInstitutions(Map.of("77777777777",
                CreditorInstitution.builder()
                    .creditorInstitutionCode("77777777777").enabled(true).build()))
            .pspBrokers(Map.of("00001", BrokerPsp.builder().enabled(true).build()))
            .creditorInstitutionStations(Map.of("00001",
                StationCreditorInstitution.builder()
                    .creditorInstitutionCode("77777777777").stationCode("00001")
                    .auxDigit(3L)
                    .segregationCode(11L)
                    .build())
            )
            .creditorInstitutionBrokers(
                Map.of("00001", BrokerCreditorInstitution.builder()
                    .enabled(true).build())
            )
        .build();
  }

}
