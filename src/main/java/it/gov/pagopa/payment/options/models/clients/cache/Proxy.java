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
public class Proxy {

  @JsonProperty("proxy_host")
  private String proxyHost = null;

  @JsonProperty("proxy_port")
  private Long proxyPort = null;

  @JsonProperty("proxy_username")
  private String proxyUsername = null;

  @JsonProperty("proxy_password")
  private String proxyPassword = null;

}
