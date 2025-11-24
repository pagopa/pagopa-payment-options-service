package it.gov.pagopa.payment.options.resources;

import it.gov.pagopa.payment.options.filters.LoggedAPI;
import it.gov.pagopa.payment.options.models.AppInfo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@LoggedAPI
@Path("/info")
@Produces(value = MediaType.APPLICATION_JSON)
public class Generic {
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "")
    private String name;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "")
    private String version;

    @ConfigProperty(name = "application.environment", defaultValue = "")
    private String environment;


    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AppInfo.class))),
            @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    @GET
    public Response info() {
        AppInfo info = AppInfo.builder()
                .name(name)
                .version(version)
                .environment(environment)
                .build();
        return Response.ok(info).build();
    }
}
