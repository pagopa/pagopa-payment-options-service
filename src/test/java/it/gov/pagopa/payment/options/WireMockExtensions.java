package it.gov.pagopa.payment.options;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
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
            get(urlEqualTo("/extensions?id=io.quarkus:quarkus-rest-client"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            objectMapper.writeValueAsString(
                                PaymentOptionsResponse.builder().build()
                            )
                        )
                )
        );

        return Map.of(
            "quarkus.rest-client.\"org.acme.rest.client.ExtensionsService\".url",
            wireMockServer.baseUrl()
        );
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();  
        }
    }
}
