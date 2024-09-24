package it.gov.pagopa.payment.options.exception;

import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import java.util.Objects;

@Getter
public class CreditorInstitutionException extends RuntimeException {

  private ErrorResponse errorResponse;

  /**
   * Constructs new exception with provided error code and message
   *
   * @param errorResponse Error Response
   * @param message   Detail message
   */
  public CreditorInstitutionException(ErrorResponse errorResponse, String message) {
    super(message);
    this.errorResponse = Objects.requireNonNull(errorResponse);
  }

  /**
   * Constructs new exception with provided error response, message and cause
   *
   * @param errorResponse Error Response
   * @param message   Detail message
   * @param cause     Exception causing the constructed one
   */
  public CreditorInstitutionException(ErrorResponse errorResponse, String message, Throwable cause) {
    super(message, cause);
    this.errorResponse = Objects.requireNonNull(errorResponse);
  }


}
