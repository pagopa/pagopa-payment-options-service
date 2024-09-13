package it.gov.pagopa.payment.options.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

/**
 * Template for the creditor institution REST client
 */
public interface CreditorInstitutionRestClientInterface {

  @GET
  @Path("/payment-options/organizations/{fiscal-code}/notices/{notice-number}")
  @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${CreditorInstitutionRestClient.ocpSubKey}")
  Response verifyPaymentOptions(
      @PathParam("fiscal-code") String fiscalCode,
      @PathParam("notice-number") String noticeNumber,
      @QueryParam("idPA") String idPA,
      @QueryParam("idBrokerPA") String idBrokerPA,
      @QueryParam("idStation") String idStation,
      @HeaderParam("X-Host-Url") String hostUrl,
      @HeaderParam("X-Host-Port") Integer hostPort,
      @HeaderParam("X-Host-Path") String hostPath
  );

}
