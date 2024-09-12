package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * StationCreditorInstitution
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationCreditorInstitution   {

  @JsonProperty("creditor_institution_code")
  private String creditorInstitutionCode = null;

  @JsonProperty("station_code")
  private String stationCode = null;

  @JsonProperty("application_code")
  private Long applicationCode = null;

  @JsonProperty("aux_digit")
  private Long auxDigit = null;

  @JsonProperty("segregation_code")
  private Long segregationCode = null;

  @JsonProperty("mod4")
  private Boolean mod4 = null;

  @JsonProperty("broadcast")
  private Boolean broadcast = null;

  @JsonProperty("primitive_version")
  private Integer primitiveVersion = null;

  @JsonProperty("spontaneous_payment")
  private Boolean spontaneousPayment = null;

}
