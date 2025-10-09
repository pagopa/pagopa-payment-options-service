package it.gov.pagopa.payment.options.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Model class for the error response */
@Getter
@Setter
@Builder
@Jacksonized
@RegisterForReflection
public class ErrorResponse {

  @Schema(examples = "500")
  private int httpStatusCode;

  @Schema(examples = "Internal Server Error")
  private String httpStatusDescription;

  @Schema(examples = "An unexpected error has occurred. Please contact support.")
  private String errorMessage;

  @Schema(examples = "ODP-<ERR_ID>")
  private String appErrorCode;

  @Schema(examples = "1724425035")
  private Long timestamp;

  @Schema(examples = "2024-08-23T14:57:15.635528")
  private String dateTime;
}
