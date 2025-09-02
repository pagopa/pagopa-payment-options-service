package it.gov.pagopa.payment.options.resources;

import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.services.PaymentOptionsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.UUID;

/**
 * Exposes REST interfaces for the payment options services
 */
@Path("/payment-options")
@Produces(value = MediaType.APPLICATION_JSON)
public class PaymentOptionsResource {

  @Inject
  public PaymentOptionsService paymentOptionsService;

  /**
   * Provides the service method to execute payment options verify process, attempting
   * to use the extracted station config data to contact the exposed creditor institution
   * Rest client
   * @param idPsp input id PSP
   * @param idBrokerPsp input id Broker PSP
   * @param organizationFiscalCode EC fiscal code
   * @param noticeNumber input notice number
   * @return instance of extracted PaymentOptions, obtained from the external creditor institution
   * REST api
   */
  @GET
  @Path("/organizations/{organization-fiscal-code}/notices/{notice-number}")
  @Operation(
      operationId="getPaymentOptions",
      summary = "Get payment options",
      description =
          "Retrieve the payment options related to the provided input"
  )
  @APIResponses(
      value = {
          @APIResponse(ref = "#/components/responses/ErrorResponse500"),
          @APIResponse(ref = "#/components/responses/ErrorResponse400"),
          @APIResponse(ref = "#/components/responses/ErrorResponse404"),
          @APIResponse(
              responseCode = "200",
              description = "Success",
              content =
              @Content(
                  mediaType = MediaType.APPLICATION_JSON,
                  schema = @Schema(implementation = PaymentOptionsResponse.class),
                  example = """
                          {
                              "organizationFiscalCode": "77777777777",
                              "companyName": "EC",
                              "officeName": "EC",
                              "paymentOptions": [
                                  {
                                      "description": "Test PayOpt - unica opzione",
                                      "numberOfInstallments": 1,
                                      "amount": 120,
                                      "dueDate": "2024-10-30T23:59:59",
                                      "validFrom": "2024-09-30T23:59:59",
                                      "status": "non pagato",
                                      "status reason": "desc",
                                      "allCCP": "false",
                                      "installments": [
                                          {
                                              "nav": "311111111111111111",
                                              "iuv": "311111111111111111",
                                              "amount": 120,
                                              "description": "Test Opt Inst - unica opzione",
                                              "dueDate": "2024-10-30T23:59:59",
                                              "validFrom": "2024-09-30T23:59:59",
                                              "status": "non pagato",
                                              "status reason": "desc"
                                          }
                                      ]
                                  }
                              ]
                          }""")
          )
      }
  )
  public RestResponse<PaymentOptionsResponse> getPaymentOptions(
      @PathParam("organization-fiscal-code") String organizationFiscalCode,
      @PathParam("notice-number") String noticeNumber,
      @QueryParam("idPsp") String idPsp,
      @Parameter(hidden = true) @QueryParam("idBrokerPsp") String idBrokerPsp,
      @HeaderParam("X-Session-Id") String sessionId
  ) {
    PaymentOptionsResponse paymentOptionsResponse =
        paymentOptionsService.getPaymentOptions(idPsp, idBrokerPsp, organizationFiscalCode, noticeNumber,
            sessionId != null ? sessionId : UUID.randomUUID().toString());
    return RestResponse.status(Status.OK, paymentOptionsResponse);
  }

}
