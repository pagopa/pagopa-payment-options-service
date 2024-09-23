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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
