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
 * CreditorInstitution
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditorInstitution {

  @JsonProperty("creditor_institution_code")
  private String creditorInstitutionCode = null;

  @JsonProperty("enabled")
  private Boolean enabled = null;

  @JsonProperty("business_name")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private String businessName = null;

  @JsonProperty("description")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private String description = null;

  @JsonProperty("address")
  @JsonInclude(JsonInclude.Include.NON_ABSENT)  // Exclude from JSON if absent
  @JsonSetter(nulls = Nulls.FAIL)    // FAIL setting if the value is null
  private CreditorInstitutionAddress address = null;

  @JsonProperty("psp_payment")
  private Boolean pspPayment = null;

  @JsonProperty("reporting_ftp")
  private Boolean reportingFtp = null;

  @JsonProperty("reporting_zip")
  private Boolean reportingZip = null;

}
