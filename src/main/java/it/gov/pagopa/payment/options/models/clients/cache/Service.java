package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Service {

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("target_host")
  private String targetHost = null;

  @JsonProperty("target_port")
  private Long targetPort = null;

  @JsonProperty("target_path")
  private String targetPath = null;

}
