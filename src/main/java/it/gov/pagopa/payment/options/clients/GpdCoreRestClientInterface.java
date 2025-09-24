package it.gov.pagopa.payment.options.clients;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST client interface for GPD-Core "verifyPaymentOptions"
 */
public interface GpdCoreRestClientInterface {
    @POST
    @Path("/payment-options/organizations/{organization-fiscal-code}/notices/{notice-number}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response verifyPaymentOptions(
            @PathParam("organization-fiscal-code") String organizationFiscalCode,
            @PathParam("notice-number") String noticeNumber,
            @QueryParam("segregationCodes") String segregationCodes
    );
}
