package it.gov.pagopa.payment.options;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import lombok.SneakyThrows;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;


public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {  

    private WireMockServer wireMockServer;

    @SneakyThrows
    @Override
    public Map<String, String> start() {
        ObjectMapper objectMapper = new ObjectMapper();
        wireMockServer = new WireMockServer();
        wireMockServer.start(); 

        wireMockServer.stubFor(
            get(urlEqualTo(
                "/payment-options/organizations/77777777777/notices/311111111112222222"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            objectMapper.writeValueAsString(
                                PaymentOptionsResponse.builder().build()
                            )
                        )
                )
        );

        wireMockServer.stubFor(
            get(urlEqualTo(
                "/payment-options/organizations/87777777777/notices/311111111112222222"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(500)
                    .withBody(
                        objectMapper.writeValueAsString(
                            ErrorResponse.builder().build()
                        )
                    )
                )
        );

        return Map.of(
            "CreditorInstitutionRestClient.apimEndpoint",
            wireMockServer.baseUrl(),
            "CreditorInstitutionRestClient.ocpSubKey", "test"
        );
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();  
        }
    }
}
