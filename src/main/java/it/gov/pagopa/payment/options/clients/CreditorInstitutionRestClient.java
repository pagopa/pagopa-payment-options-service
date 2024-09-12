package it.gov.pagopa.payment.options.clients;

import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsRequest;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import java.net.MalformedURLException;
import java.net.URL;

@ApplicationScoped
public class CreditorInstitutionRestClient {

  public PaymentOptionsResponse callEcPaymentOptionsVerify(
      String endpoint, String proxyHost, Long proxyPort,
      String targetHost, Long targetPort, String targetPath,
      PaymentOptionsRequest request) throws MalformedURLException {

    RestClientBuilder builder =
        RestClientBuilder.newBuilder().baseUrl(new URL(endpoint));
    if (proxyHost != null && proxyPort != null) {
      builder = builder.proxyAddress(proxyHost, proxyPort.intValue());
    }
    CreditorInstitutionRestClientInterface ecRestClientInterface = builder.build(
        CreditorInstitutionRestClientInterface.class);
    return ecRestClientInterface.verifyPaymentOptions(
        request, targetHost, targetPort.intValue(), targetPath);

  }

}
