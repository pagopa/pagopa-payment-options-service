package it.gov.pagopa.payment.options.resources;

import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.services.PaymentOptionsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/payment-options")
@Produces(value = MediaType.APPLICATION_JSON)
public class PaymentOptionsResource {

  @Inject
  public PaymentOptionsService paymentOptionsService;

  @GET
  @Path("/organizations/{fiscal-code}/notices/{notice-number}")
  public RestResponse<PaymentOptionsResponse> getPaymentOptions(
      @PathParam("fiscal-code") String fiscalCode,
      @PathParam("notice-number") String noticeNumber,
      @QueryParam("idPsp") String idPsp,
      @QueryParam("idBrokerPsp") String idBrokerPsp
  ) {
    PaymentOptionsResponse paymentOptionsResponse =
        paymentOptionsService.getPaymentOptions(idPsp, idBrokerPsp, fiscalCode, noticeNumber);

    return RestResponse.status(Status.OK, paymentOptionsResponse);
  }

}