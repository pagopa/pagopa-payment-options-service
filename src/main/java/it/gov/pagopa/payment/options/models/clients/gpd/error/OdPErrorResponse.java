package it.gov.pagopa.payment.options.models.clients.gpd.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OdPErrorResponse {
  private Integer httpStatusCode;        // e.g. 404
  private String  httpStatusDescription; // e.g. "Not Found"
  private String  appErrorCode;          // e.g. "ODP-107"
  private Long    timestamp;
  private String  dateTime;
  private String  errorMessage;          // e.g. "PAA_* ..."
}
