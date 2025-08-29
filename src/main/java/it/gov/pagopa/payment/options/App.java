package it.gov.pagopa.payment.options;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.QuarkusMain;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.ServerVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@OpenAPIDefinition(
        components =
        @Components(
                securitySchemes = {
                        @SecurityScheme(
                                securitySchemeName = "ApiKey",
                                apiKeyName = "Ocp-Apim-Subscription-Key",
                                in = SecuritySchemeIn.HEADER,
                                type = SecuritySchemeType.APIKEY)
                },
                responses = {
                        @APIResponse(
                                name = "ErrorResponse500",
                                responseCode = "500",
                                description = "Internal Server Error",
                                content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        examples = {
                                                @ExampleObject(
                                                        name = "InternalServerErrorExample",
                                                        value = """
                                                                {
                                                                  "httpStatusCode": 500,
                                                                  "httpStatusDescription": "Internal Server Error",
                                                                  "errorMessage": "An unexpected error has occurred. Please contact support.",
                                                                  "appErrorCode": "ODP-<ERR_ID>",
                                                                  "timestamp": 1724425035,
                                                                  "dateTime": "2024-08-23T14:57:15.635528"
                                                                }"""
                                                )
                                        },
                                        schema = @Schema(implementation = ErrorResponse.class))),
                        @APIResponse(
                                name = "ErrorResponse400",
                                responseCode = "400",
                                description = "Default app exception for status 400",
                                content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        examples = {
                                                @ExampleObject(
                                                        name = "BadRequestExample",
                                                        value = """
                                                                {
                                                                  "httpStatusCode": 400,
                                                                  "httpStatusDescription": "Bad Request",
                                                                  "errorMessage": "Invalid request. Please check the submitted data and try again.",
                                                                  "appErrorCode": "ODP-<ERR_ID>",
                                                                  "timestamp": 1724425035,
                                                                  "dateTime": "2024-08-23T14:57:15.635528"
                                                                }"""
                                                )
                                        },
                                        schema = @Schema(implementation = ErrorResponse.class))),

                        @APIResponse(
                                name = "ErrorResponse404",
                                responseCode = "404",
                                description = "Default app exception for status 404",
                                content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        examples = {
                                                @ExampleObject(
                                                        name = "NotFoundExample",
                                                        value = """
                                                                {
                                                                  "httpStatusCode": 404,
                                                                  "httpStatusDescription": "Not Found",
                                                                  "errorMessage": "Resource not found. Please verify the request and try again.",
                                                                  "appErrorCode": "ODP-<ERR_ID>",
                                                                  "timestamp": 1724425035,
                                                                  "dateTime": "2024-08-23T14:57:15.635528"
                                                                }"""
                                                )
                                        },
                                        schema = @Schema(implementation = ErrorResponse.class)
                                ))
                }),
        info = @Info(
                title = "Payments Options Services",
                version = "",
                description = "placeholder-for-replace"),
        servers = {
                @Server(url = "http://localhost:8080", description = "Localhost base URL"),
                @Server(url = "https://{host}/odp/service/v1", description = "Base URL",
                        variables = {
                                @ServerVariable(name = "host",
                                        enumeration = {"api.dev.platform.pagopa.it", "api.uat.platform.pagopa.it", "api.platform.pagopa.it"},
                                        defaultValue = "api.dev.platform.pagopa.it")})
        }
)
@Startup
@QuarkusMain
public class App extends Application {

    public static void main(String[] args) {
        Quarkus.run(QuarkusApp.class, args);
    }

    public static class QuarkusApp implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            Logger logger = LoggerFactory.getLogger(QuarkusApp.class);
            logger.info("QuarkusApp Run");
            Quarkus.waitForExit();
            return 0;
        }
    }

}
