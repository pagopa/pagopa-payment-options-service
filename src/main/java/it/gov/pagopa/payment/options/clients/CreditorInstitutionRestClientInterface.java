package it.gov.pagopa.payment.options.clients;

import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsRequest;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

@Path("")
public interface CreditorInstitutionRestClientInterface {

  @POST
  @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${EcRestClient.ocpSubKey}")
  PaymentOptionsResponse verifyPaymentOptions(PaymentOptionsRequest paymentOptionsRequest,
      @HeaderParam("X-Host-Url") String hostUrl,
      @HeaderParam("X-Host-Port") Integer hostPort,
      @HeaderParam("X-Host-Path") String hostPath
  );

}
