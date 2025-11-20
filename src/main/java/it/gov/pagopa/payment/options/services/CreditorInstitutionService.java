package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.CreditorInstitutionRestClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service containing methods to manage call to the creditor institution REST service
 */
@ApplicationScoped
public class CreditorInstitutionService {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionService.class);

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
  String APIM_FORWARDER_ENDPOINT;

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimPath")
  Optional<String> APIM_FORWARDER_PATH;

  // endpoint "special guest" GPD-Core
  @ConfigProperty(name = "CreditorInstitutionRestClient.gpdRestEndpoint")
  Optional<String> GPD_REST_ENDPOINT;
  @ConfigProperty(name = "CreditorInstitutionRestClient.odpBasePathPaymentOptionsServices")
  Optional<String> ODP_BASE_PATH_PAYMENT_OPTIONS_SERVICES;

  static String PAYMENT_OPTIONS_SERVICE_SUFFIX = "/payment-options/organizations/%s/notices/%s";

  @Inject
  CreditorInstitutionRestClient creditorInstitutionRestClient;

  /**
   * Using the provided input attempts to call the creditor institution service
   * to obtain the list paymentOptions related to the input
   *
   * The method contains checks regarding the endpoint to use, and attempts to
   * extract the REST target params
   *
   * @param noticeNumber input notice number
   * @param fiscalCode input fiscal code
   * @param station station containing the connection config to use
   * @return
   */
  public PaymentOptionsResponse getPaymentOptions(
		  String noticeNumber, String fiscalCode, Station station) {

	  // 1) Special guest: EC = GPD-Core
	  if (isEcGpdSpecialGuest(station)) {
		  logger.info("[Payment Options] Using GPD-Core special guest endpoint for station {}",
				  station.getStationCode());

		  String gpdEndpoint = station.getRestEndpoint();
		  if (gpdEndpoint == null) {
			  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA,
					  "[Payment Options] GPD-Core endpoint not provided for station "
							  + station.getStationCode());
		  }

		  try {
			  return creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
					  gpdEndpoint,
					  fiscalCode,
					  noticeNumber,
					  null  // segregationCodes is not passed (optional param)
					  );
		  } catch (MalformedURLException e) {
			  throw new PaymentOptionsException(
					  AppErrorCodeEnum.ODP_SEMANTICA,
					  String.format(
							  "[Payment Options] Malformed GPD-Core URL for station %s: %s",
							  station.getStationCode(),
							  gpdEndpoint
							  ),
					  e
					  );
		  }
	  }

	  // 2) "Normal" behavior: EC via APIM forwarder
	  if (station.getConnection().getIp() == null ||
			  !APIM_FORWARDER_ENDPOINT.contains(station.getConnection().getIp())) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
				  "[Payment Options] Station not configured to pass through the APIM Forwarder");
	  }

	  String endpoint = getEndpoint(station, APIM_FORWARDER_PATH.orElse(""));

	  if (station.getRestEndpoint() == null) {
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA,
				  "[Payment Options] Station new verify endpoint not provided");
	  }

	  String targetHost;
	  long targetPort;
	  String targetPath;
	  try {
		  String[] verifyEndpointParts =
				  station.getRestEndpoint().split("/", 4);
		  targetHost = verifyEndpointParts[2];
		  String[] hostSplit = verifyEndpointParts[2].split(":");
		  targetPort = hostSplit.length > 1 ?
				  Long.parseLong(hostSplit[1]) :
					  verifyEndpointParts[0].contains(ProtocolEnum.HTTPS.name().toLowerCase()) ?
							  443L : 80L;
		  String formattedPath =  String.format(PAYMENT_OPTIONS_SERVICE_SUFFIX, fiscalCode, noticeNumber);
		  targetPath = verifyEndpointParts.length > 3 ? verifyEndpointParts[3].concat(formattedPath) : formattedPath;
	  } catch (Exception e) {
		  logger.error("[Payment Options] Malformed Target URL: {}", e.getMessage());
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA, e.getMessage());
	  }

	  try {
		  return creditorInstitutionRestClient.callEcPaymentOptionsVerify(
				  endpoint,
				  station.getProxy() != null ? station.getProxy().getProxyHost() : null,
						  station.getProxy() != null ? station.getProxy().getProxyPort() : null,
								  targetHost, targetPort, targetPath,
								  fiscalCode, noticeNumber
				  );
	  } catch (MalformedURLException e) {
		  logger.error("[Payment Options] Malformed URL: {}", e.getMessage());
		  throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA, e.getMessage());
	  }

  }

  private static String getEndpoint(Station station, String apimForwarderPath) {
    return (station.getConnection().getProtocol() != null &&
        (station.getConnection().getProtocol().equals(ProtocolEnum.HTTPS)) ?
        ProtocolEnum.HTTPS.name().toLowerCase() :
        station.getConnection().getProtocol().name().toLowerCase()) +
            "://" + station.getConnection().getIp() + ":" +
            (station.getConnection().getPort() != null ?
                String.valueOf(station.getConnection().getPort()) : "80")
                .concat(apimForwarderPath);
  }
  
  private boolean isEcGpdSpecialGuest(Station station) {
	  if (station.getRestEndpoint() == null || GPD_REST_ENDPOINT.isEmpty()) {
		  return false;
	  }
	  // removes possible final "/"
	  // used "/++$" (possessive quantifier) ​​for sonar reporting java:S5852
	  String stationEndpoint = station.getRestEndpoint().replaceAll("/++$", "");
	  String gpdEndpoint = GPD_REST_ENDPOINT.get().replaceAll("/++$", "");
	  return stationEndpoint.equalsIgnoreCase(gpdEndpoint);
  }

}
