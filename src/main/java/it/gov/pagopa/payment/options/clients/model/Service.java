package it.gov.pagopa.payment.options.clients.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
