package it.gov.pagopa.payment.options.models.clients.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PSP
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentServiceProvider {

  @JsonProperty("psp_code")
  private String pspCode;

  @JsonProperty("enabled")
  private boolean enabled;

  @JsonProperty("description")
  private String description;

  @JsonProperty("business_name")
  private String businessName;

  @JsonProperty("abi")
  private String abi;

  @JsonProperty("bic")
  private String bic;

  @JsonProperty("my_bank_code")
  private String myBankCode;

  @JsonProperty("digital_stamp")
  private Boolean digitalStamp;

  @JsonProperty("agid_psp")
  private boolean agidPsp;

  @JsonProperty("tax_code")
  private String taxCode;

  @JsonProperty("vat_number")
  private String vatNumber;

}
