package it.gov.pagopa.payment.options.clients.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigDataV1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDataV1 {

  @JsonProperty("version")
  @Builder.Default
  private String version = null;

  @JsonProperty("creditorInstitutions")
  @Valid
  private Map<String, CreditorInstitution> creditorInstitutions = new HashMap<String, CreditorInstitution>();

  @JsonProperty("stations")
  @Valid
  private Map<String, Station> stations = new HashMap<String, Station>();

//  @JsonProperty("creditorInstitutionBrokers")
//  @Valid
//  private Map<String, BrokerCreditorInstitution> creditorInstitutionBrokers = new HashMap<String, BrokerCreditorInstitution>();
//
//  @JsonProperty("creditorInstitutionStations")
//  @Valid
//  private Map<String, StationCreditorInstitution> creditorInstitutionStations = new HashMap<String, StationCreditorInstitution>();
//
//  @JsonProperty("encodings")
//  @Valid
//  private Map<String, Encoding> encodings = new HashMap<String, Encoding>();
//
//  @JsonProperty("creditorInstitutionEncodings")
//  @Valid
//  private Map<String, CreditorInstitutionEncoding> creditorInstitutionEncodings = new HashMap<String, CreditorInstitutionEncoding>();
//
//  @JsonProperty("ibans")
//  @Valid
//  private Map<String, Iban> ibans = new HashMap<String, Iban>();
//
//  @JsonProperty("creditorInstitutionInformations")
//  @Valid
//  private Map<String, CreditorInstitutionInformation> creditorInstitutionInformations = new HashMap<String, CreditorInstitutionInformation>();
//
//  @JsonProperty("psps")
//  @Valid
//  private Map<String, PaymentServiceProvider> psps = new HashMap<String, PaymentServiceProvider>();
//
//  @JsonProperty("pspBrokers")
//  @Valid
//  private Map<String, BrokerPsp> pspBrokers = new HashMap<String, BrokerPsp>();
//
//  @JsonProperty("paymentTypes")
//  @Valid
//  private Map<String, PaymentType> paymentTypes = new HashMap<String, PaymentType>();
//
//  @JsonProperty("pspChannelPaymentTypes")
//  @Valid
//  private Map<String, PspChannelPaymentType> pspChannelPaymentTypes = new HashMap<String, PspChannelPaymentType>();
//
//  @JsonProperty("plugins")
//  @Valid
//  private Map<String, Plugin> plugins = new HashMap<String, Plugin>();
//
//  @JsonProperty("pspInformationTemplates")
//  @Valid
//  private Map<String, PspInformation> pspInformationTemplates = new HashMap<String, PspInformation>();
//
//  @JsonProperty("pspInformations")
//  @Valid
//  private Map<String, PspInformation> pspInformations = new HashMap<String, PspInformation>();
//
//  @JsonProperty("channels")
//  @Valid
//  private Map<String, Channel> channels = new HashMap<String, Channel>();
//
//  @JsonProperty("cdsServices")
//  @Valid
//  private Map<String, CdsService> cdsServices = new HashMap<String, CdsService>();
//
//  @JsonProperty("cdsSubjects")
//  @Valid
//  private Map<String, CdsSubject> cdsSubjects = new HashMap<String, CdsSubject>();
//
//  @JsonProperty("cdsSubjectServices")
//  @Valid
//  private Map<String, CdsSubjectService> cdsSubjectServices = new HashMap<String, CdsSubjectService>();
//
//  @JsonProperty("cdsCategories")
//  @Valid
//  private Map<String, CdsCategory> cdsCategories = new HashMap<String, CdsCategory>();
//
//  @JsonProperty("configurations")
//  @Valid
//  private Map<String, ConfigurationKey> configurations = new HashMap<String, ConfigurationKey>();
//
//  @JsonProperty("ftpServers")
//  @Valid
//  private Map<String, FtpServer> ftpServers = new HashMap<String, FtpServer>();
//
//  @JsonProperty("languages")
//  @Valid
//  private Map<String, String> languages = new HashMap<String, String>();
//
//  @JsonProperty("gdeConfigurations")
//  @Valid
//  private Map<String, GdeConfiguration> gdeConfigurations = new HashMap<String, GdeConfiguration>();
//
//  @JsonProperty("metadataDict")
//  @Valid
//  private Map<String, MetadataDict> metadataDict = new HashMap<String, MetadataDict>();

}
