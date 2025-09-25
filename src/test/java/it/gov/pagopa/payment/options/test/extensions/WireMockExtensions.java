package it.gov.pagopa.payment.options.test.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
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
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
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
                get(urlEqualTo("/cache?keys=stations&keys=creditorInstitutions"
                        + "&keys=psps&keys=creditorInstitutionStations&keys=pspBrokers"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withResponseBody(
                                        new Body(objectMapper.writeValueAsString(
                                                ConfigDataV1
                                                        .builder()
                                                        .psps(Map.of("00001",
                                                                PaymentServiceProvider.builder().enabled(true).build()))
                                                        .stations(Map.of("00001",
                                                                Station.builder().enabled(true).verifyPaymentOptionEnabled(true)
                                                                        .connection(Connection.builder()
                                                                                .protocol(ProtocolEnum.HTTP).ip("localhost")
                                                                                .port((long) wireMockServer.port()).build())
                                                                        .restEndpoint("https://localhost:9095")
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
                                                        .build()
                                        ))
                                )
                        ));

        wireMockServer.stubFor(
                post(urlEqualTo(
                        "/forward"))
                        .withHeader("X-Host-Path", equalTo("/payment-options/organizations/97777777777/notices/311111111112222222"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("AAAAAAAA")
                        )
        );

        wireMockServer.stubFor(
                post(urlEqualTo(
                        "/forward"))
                        .withHeader("X-Host-Path", equalTo("/payment-options/organizations/77777777777/notices/311111111112222222"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withResponseBody(
                                        new Body(objectMapper.writeValueAsString(
                                                PaymentOptionsResponse.builder().standin(true).build()
                                        ))
                                )
                        )
        );

        wireMockServer.stubFor(
                post(urlEqualTo(
                        "/forward"))
                        .withHeader("X-Host-Path", equalTo("/payment-options/organizations/87777777777/notices/311111111112222222"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(412)
                                .withResponseBody(
                                        new Body(objectMapper.writeValueAsString(
                                                ErrorResponse.builder()
                                                        .httpStatusCode(500)
                                                        .httpStatusDescription("Error")
                                                        .appErrorCode("ODB_ERRID")
                                                        .errorMessage("TEST")
                                                        .build()
                                        ))
                                )
                        )
        );

        wireMockServer.stubFor(
                post(urlEqualTo(
                        "/forward"))
                        .withHeader("X-Host-Path", equalTo("/payment-options/organizations/67777777777/notices/311111111112222222"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(404)
                                .withResponseBody(
                                        new Body(objectMapper.writeValueAsString(
                                                ErrorResponse.builder()
                                                        .httpStatusCode(404)
                                                        .httpStatusDescription("Bad Request")
                                                        .appErrorCode("ODP-107")
                                                        .errorMessage("PAA_PAGAMENTO_SCONOSCIUTO Errore per pagamento sconosciuto")
                                                        .dateTime(LocalDateTime.now().toString())
                                                        .timestamp(LocalDateTime.now().getLong(ChronoField.MILLI_OF_SECOND))
                                                        .build()
                                        ))
                                )
                        )
        );

        wireMockServer.stubFor(
                post(urlEqualTo(
                        "/forward"))
                        .withHeader("X-Host-Path", equalTo("/payment-options/organizations/57777777777/notices/311111111112222222"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(500)
                                .withResponseBody(
                                        new Body(objectMapper.writeValueAsString(
                                                ErrorResponse.builder()
                                                        .httpStatusCode(500)
                                                        .httpStatusDescription("Bad Request")
                                                        .appErrorCode("ODP-107")
                                                        .errorMessage("PAA_PAGAMENTO_SCONOSCIUTO Errore per pagamento sconosciuto")
                                                        .dateTime(LocalDateTime.now().toString())
                                                        .timestamp(LocalDateTime.now().getLong(ChronoField.MILLI_OF_SECOND))
                                                        .build()
                                        ))
                                )
                        )
        );

        return Map.of(
                "quarkus.rest-client.\"it.gov.pagopa.payment.options.clients.ApiConfigCacheClient\".url",
                wireMockServer.baseUrl(),
                "ApiConfigCacheClient.ocpSubKey", "test",
                "CreditorInstitutionRestClient.apimEndpoint",
                wireMockServer.baseUrl(),
                "CreditorInstitutionRestClient.apimPath", "",
                "CreditorInstitutionRestClient.ocpSubKey", "test",
                "wiremock.port", String.valueOf(wireMockServer.port())
        );
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
