package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Timeouts
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timeouts {

  @JsonProperty("timeout_a")
  private Long timeoutA = null;

  @JsonProperty("timeout_b")
  private Long timeoutB = null;

  @JsonProperty("timeout_c")
  private Long timeoutC = null;

}
