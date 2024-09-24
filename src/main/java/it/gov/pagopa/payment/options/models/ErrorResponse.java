package it.gov.pagopa.payment.options.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Model class for the error response
 */
@Getter
@Builder
@Jacksonized
@RegisterForReflection
public class ErrorResponse {

    @Schema(example = "500")
    private int httpStatusCode;

    @Schema(example = "Internal Server Error")
    private String httpStatusDescription;

    @Schema(example = "An unexpected error has occurred. Please contact support.")
    private String errorMessage;

    @Schema(example = "ODP-<ERR_ID>")
    private String appErrorCode;

    @Schema(example = "1724425035")
    private Long timestamp;

    @Schema(example = "2024-08-23T14:57:15.635528")
    private String dateTime;

}
