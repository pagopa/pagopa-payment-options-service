package it.gov.pagopa.payment.options.clients.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

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
