package it.gov.pagopa.payment.options.exception;

import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import java.util.Objects;

/**
 * Base exception for PaymentOptions exceptions
 */
@Getter
public class PaymentOptionsException extends RuntimeException {

    /**
     * Error code of this exception
     * -- GETTER --
     * Returns error code
     *
     * @return Error code of this exception
     */
    private final AppErrorCodeEnum errorCode;

    /**
     * Constructs new exception with provided error code and message
     *
     * @param errorCode Error code
     * @param message   Detail message
     */
    public PaymentOptionsException(AppErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    /**
     * Constructs new exception with provided error code, message and cause
     *
     * @param errorCode Error code
     * @param message   Detail message
     * @param cause     Exception causing the constructed one
     */
    public PaymentOptionsException(AppErrorCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    public static Response.Status getHttpStatus(PaymentOptionsException e) {
        return e.getErrorCode().getStatus();
    }

}
