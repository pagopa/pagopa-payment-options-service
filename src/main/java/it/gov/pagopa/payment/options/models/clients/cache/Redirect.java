package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Redirect
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Redirect {

  /**
   * Gets or Sets protocol
   */
  public enum ProtocolEnum {
    HTTPS("HTTPS"),
    
    HTTP("HTTP");

    private String value;

    ProtocolEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ProtocolEnum fromValue(String text) {
      for (ProtocolEnum b : ProtocolEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("protocol")
  private ProtocolEnum protocol = null;

  @JsonProperty("ip")
  private String ip = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("port")
  private Long port = null;

  @JsonProperty("query_string")
  private String queryString = null;

}
