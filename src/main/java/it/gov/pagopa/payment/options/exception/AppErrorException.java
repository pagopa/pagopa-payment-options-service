package it.gov.pagopa.payment.options.exception;

import lombok.Getter;

/**
 * Base exception for Payment Options exceptions
 */
@Getter
public class AppErrorException extends RuntimeException {

    public AppErrorException(Throwable error) {
        super(error);
    }

}
