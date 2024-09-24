package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BrokerCreditorInstitution
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerCreditorInstitution   {

  @JsonProperty("broker_code")
  private String brokerCode = null;

  @JsonProperty("enabled")
  private Boolean enabled = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("extended_fault_bean")
  private Boolean extendedFaultBean = null;

}
