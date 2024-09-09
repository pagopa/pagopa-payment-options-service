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
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private Connection connectionMod4 = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("redirect")
  private Redirect redirect = null;

  @JsonProperty("service")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private Service service = null;

  @JsonProperty("service_pof")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private Service servicePof = null;

  @JsonProperty("service_mod4")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private Service serviceMod4 = null;

  @JsonProperty("broker_code")
  private String brokerCode = null;

  @JsonProperty("proxy")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
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
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private Boolean flagStandin = null;

}
