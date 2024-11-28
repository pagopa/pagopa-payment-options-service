package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrokerPsp {

  @JsonProperty("broker_psp_code")
  private String brokerPspCode;

  @JsonProperty("description")
  private String description;

  @JsonProperty("enabled")
  private boolean enabled;

  @JsonProperty("extended_fault_bean")
  private boolean extendedFaultBean;

}
