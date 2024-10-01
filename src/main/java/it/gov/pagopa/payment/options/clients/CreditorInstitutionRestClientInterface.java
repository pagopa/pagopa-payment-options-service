package it.gov.pagopa.payment.options.clients;

import it.gov.pagopa.payment.options.models.events.odpRe.Properties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

/**
 * Template for the creditor institution REST client
 */
public interface CreditorInstitutionRestClientInterface {

  @POST
  @Path("/forward")
  @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${CreditorInstitutionRestClient.ocpSubKey}")
  Response verifyPaymentOptions(
      @HeaderParam("X-Host-Url") String hostUrl,
      @HeaderParam("X-Host-Port") Integer hostPort,
      @HeaderParam("X-Host-Path") String hostPath
  );

}
