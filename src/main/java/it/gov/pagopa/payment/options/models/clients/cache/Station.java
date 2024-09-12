package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Station
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {

  @JsonProperty("station_code")
  private String stationCode = null;

  @JsonProperty("enabled")
  private Boolean enabled = null;

  @JsonProperty("version")
  private Long version = null;

  @JsonProperty("connection")
  private Connection connection = null;

  @JsonProperty("connection_mod4")
  private Connection connectionMod4 = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("redirect")
  private Redirect redirect = null;

  @JsonProperty("service")
  private Service service = null;

  @JsonProperty("service_pof")
  private Service servicePof = null;

  @JsonProperty("service_mod4")
  private Service serviceMod4 = null;

  @JsonProperty("broker_code")
  private String brokerCode = null;

  @JsonProperty("proxy")
  private Proxy proxy = null;

  @JsonProperty("thread_number")
  private Long threadNumber = null;

  @JsonProperty("timeouts")
  private Timeouts timeouts = null;

  @JsonProperty("invio_rt_istantaneo")
  private Boolean invioRtIstantaneo = null;

  @JsonProperty("primitive_version")
  private Integer primitiveVersion = null;

  @JsonProperty("flag_standin")
  private Boolean flagStandin = null;

  @JsonProperty("verify_payment_option_enabled")
  private Boolean verifyPaymentOptionEnabled = false;

  @JsonProperty("verify_payment_option_endpoint")
  private String verifyPaymentOptionEndpoint;

}
