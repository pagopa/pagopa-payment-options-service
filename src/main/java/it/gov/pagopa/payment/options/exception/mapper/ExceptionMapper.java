package it.gov.pagopa.payment.options.exception.mapper;

import static it.gov.pagopa.payment.options.exception.PaymentOptionsException.getHttpStatus;
import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.INTERNAL_SERVER_ERROR;

import io.smallrye.mutiny.CompositeException;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import java.util.List;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionMapper {

    Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);

    private ErrorResponse buildErrorResponse(Response.Status status, AppErrorCodeEnum errorCode, String message) {
        return ErrorResponse.builder()
                .httpStatusCode(status.getStatusCode())
                .httpStatusDescription(status.getReasonPhrase())
                .appErrorCode(errorCode.getErrorCode())
                .errorMessage(errorCode.getErrorMessage())
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }

    @ServerExceptionMapper
    public Response mapCompositeException(CompositeException exception) {
        logger.error(exception.getMessage(), exception);
        Exception composedException;
        List<Throwable> causes = exception.getCauses();
        composedException = (Exception) causes.get(causes.size() - 1);

        if(composedException instanceof NotFoundException ex) {
            return mapNotFoundException(ex);
        } else if(composedException instanceof PaymentOptionsException paymentNoticeException) {
          return mapPaymentNoticeException(paymentNoticeException);
        } else if(composedException instanceof CreditorInstitutionException creditorInstitutionException) {
          return mapCreditorInstitutionException(creditorInstitutionException);
        } else {
            return mapGenericException(exception);
        }

    }

  @ServerExceptionMapper
  private Response mapCreditorInstitutionException(CreditorInstitutionException creditorInstitutionException) {
    logger.error(creditorInstitutionException.getMessage(), creditorInstitutionException);
    return Response.status(creditorInstitutionException.getErrorResponse().getHttpStatusCode())
        .entity(creditorInstitutionException.getErrorResponse()).build();
  }

  @ServerExceptionMapper
  public Response mapNotFoundException(NotFoundException exception) {
      logger.error(exception.getMessage(), exception);
      return Response.status(NOT_FOUND).entity(
              buildErrorResponse(Status.NOT_FOUND, AppErrorCodeEnum.ODP_SINTASSI,
                      "Invalid parameters on request")).build();
  }

  @ServerExceptionMapper
  public Response mapPaymentNoticeException(PaymentOptionsException exception) {
      logger.error(exception.getMessage(), exception);
      Response.Status status = getHttpStatus(exception);
      return Response.status(status).entity(buildErrorResponse(status,
              exception.getErrorCode(), exception.getMessage())).build();
  }

  @ServerExceptionMapper
  public Response mapGenericException(Exception exception) {
      logger.error(exception.getMessage(), exception);
      return Response.status(INTERNAL_SERVER_ERROR)
              .entity(buildErrorResponse(
                      Response.Status.INTERNAL_SERVER_ERROR,
                      AppErrorCodeEnum.ODP_SYSTEM_ERROR,
                      "Unexpected Error"))
              .build();
  }

}
