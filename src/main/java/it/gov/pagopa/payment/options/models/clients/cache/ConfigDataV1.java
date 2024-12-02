package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigDataV1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDataV1 {

  @JsonProperty("version")
  @Builder.Default
  private String version = null;

  @JsonProperty("creditorInstitutions")
  @Valid
  private Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();

  @JsonProperty("stations")
  @Valid
  private Map<String, Station> stations = new HashMap<>();

  @JsonProperty("creditorInstitutionBrokers")
  @Valid
  private Map<String, BrokerCreditorInstitution> creditorInstitutionBrokers = new HashMap<>();

  @JsonProperty("creditorInstitutionStations")
  @Valid
  private Map<String, StationCreditorInstitution> creditorInstitutionStations = new HashMap<>();

  @JsonProperty("psps")
  @Valid
  private Map<String, PaymentServiceProvider> psps = new HashMap<>();

  @JsonProperty("pspBrokers")
  @Valid
  private Map<String, BrokerPsp> pspBrokers = new HashMap<>();
}
