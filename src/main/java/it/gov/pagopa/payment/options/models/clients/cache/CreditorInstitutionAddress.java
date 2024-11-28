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
public class CreditorInstitutionAddress {

  @JsonProperty("location")
  private String location = null;

  @JsonProperty("city")
  private String city = null;

  @JsonProperty("zip_code")
  private String zipCode = null;

  @JsonProperty("country_code")
  private String countryCode = null;

  @JsonProperty("tax_domicile")
  private String taxDomicile = null;

}
