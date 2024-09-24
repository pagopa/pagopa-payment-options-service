package it.gov.pagopa.payment.options.models.enums;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;

/**
 * Enumeration for application error codes and messages
 */
@Getter
public enum AppErrorCodeEnum {

    ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE(Status.SERVICE_UNAVAILABLE, "ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE", "Required station is currently unavailable through the provided config params"),
    ODP_SEMANTICA(Response.Status.BAD_REQUEST, "ODP_SINTASSI", "Bad request error on input syntax"),
    ODP_SINTASSI(Response.Status.BAD_REQUEST, "ODP_SEMANTICA", "Bad request error due to semantic error withing the verify flow"),
    ODP_SYSTEM_ERROR(Response.Status.INTERNAL_SERVER_ERROR, "ODP_SYSTEM_ERROR", "Unexpected system Error"),

    PA_SYSTEM_ERROR(Response.Status.INTERNAL_SERVER_ERROR, "PA_SYSTEM_ERROR", "Unexpected error on EC system call");


    private final Response.Status status;
    private final String errorCode;
    private final String errorMessage;

    AppErrorCodeEnum(Response.Status status, String errorCode, String errorMessage) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
