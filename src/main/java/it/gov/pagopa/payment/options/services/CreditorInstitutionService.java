package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.clients.CreditorInstitutionRestClient;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.cache.Connection;
import it.gov.pagopa.payment.options.models.clients.cache.Connection.ProtocolEnum;
import it.gov.pagopa.payment.options.models.clients.cache.Station;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service containing methods to manage call to the creditor institution REST service */
@ApplicationScoped
public class CreditorInstitutionService {

  private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionService.class);

  private static final String PAYMENT_OPTIONS_SERVICE_SUFFIX =
      "/payment-options/organizations/%s/notices/%s";

  private final String apimForwarderEndpoint;
  private final String apimForwarderPath;
  private final CreditorInstitutionRestClient creditorInstitutionRestClient;

  CreditorInstitutionService(
      @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
          String apimForwarderEndpoint,
      @ConfigProperty(name = "CreditorInstitutionRestClient.apimPath") String apimForwarderPath,
      CreditorInstitutionRestClient creditorInstitutionRestClient) {
    this.apimForwarderEndpoint = apimForwarderEndpoint;
    this.apimForwarderPath = apimForwarderPath;
    this.creditorInstitutionRestClient = creditorInstitutionRestClient;
  }
  
 //endpoint "special guest" GPD-Core
 @ConfigProperty(name = "CreditorInstitutionRestClient.gpdRestEndpoint")
 Optional<String> GPD_REST_ENDPOINT;
 @ConfigProperty(name = "CreditorInstitutionRestClient.odpBasePathPaymentOptionsServices")
 Optional<String> ODP_BASE_PATH_PAYMENT_OPTIONS_SERVICES;

  /**
   * Using the provided input attempts to call the creditor institution service to obtain the list
   * paymentOptions related to the input
   *
   * <p>The method contains checks regarding the endpoint to use, and attempts to extract the REST
   * target params
   *
   * @param noticeNumber input notice number
   * @param fiscalCode input fiscal code
   * @param station station containing the connection config to use
   * @return the payment option retrieved from creditor institution
   */
 public PaymentOptionsResponse getPaymentOptions(
		 String noticeNumber, String fiscalCode, Station station, Long segregationCode) {

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

		 return creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
				 gpdEndpoint,
				 fiscalCode,
				 noticeNumber,
				 (segregationCode != null) ? String.valueOf(segregationCode) : null  // optional param
				 );
	 }

	 // 2) "Normal" behavior: EC via APIM forwarder
	 if (station.getConnection().getIp() == null
			 || !this.apimForwarderEndpoint.contains(station.getConnection().getIp())) {
		 throw new PaymentOptionsException(
				 AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
				 "[Payment Options] Station not configured to pass through the APIM Forwarder");
	 }

	 if (station.getRestEndpoint() == null) {
		 throw new PaymentOptionsException(
				 AppErrorCodeEnum.ODP_SEMANTICA,
				 "[Payment Options] Station new verify endpoint not provided");
	 }

	 String targetHost;
	 long targetPort;
	 String targetPath;
	 try {
		 String[] verifyEndpointParts = station.getRestEndpoint().split("/", 4);
		 targetHost = verifyEndpointParts[2];
		 targetPort = getTargetPort(verifyEndpointParts);
		 String formattedPath =
				 String.format(PAYMENT_OPTIONS_SERVICE_SUFFIX, fiscalCode, noticeNumber);
		 targetPath =
				 verifyEndpointParts.length > 3
				 ? verifyEndpointParts[3].concat(formattedPath)
						 : formattedPath;
	 } catch (Exception e) {
		 logger.error("[Payment Options] Malformed Target URL", e);
		 throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA, e.getMessage());
	 }

	 URL forwarderUrl = buildForwarderUrl(station.getConnection());
	 return this.creditorInstitutionRestClient.callEcPaymentOptionsVerify(
			 forwarderUrl,
			 station.getProxy() != null ? station.getProxy().getProxyHost() : null,
					 station.getProxy() != null ? station.getProxy().getProxyPort() : null,
							 targetHost,
							 targetPort,
							 targetPath);
 }

  private long getTargetPort(String[] verifyEndpointParts) {
    String[] hostSplit = verifyEndpointParts[2].split(":");

    if (hostSplit.length > 1) {
      return Long.parseLong(hostSplit[1]);
    }
    if (verifyEndpointParts[0].contains(ProtocolEnum.HTTPS.name().toLowerCase())) {
      return 443L;
    }
    return 80L;
  }

  private URL buildForwarderUrl(@Nonnull Connection connection) {
    try {
      String scheme = getProtocol(connection);
      String port = connection.getPort() != null ? String.valueOf(connection.getPort()) : "80";
      String url =
          String.format("%s://%s:%s%s", scheme, connection.getIp(), port, this.apimForwarderPath);

      return new URL(url);
    } catch (MalformedURLException e) {
      logger.error("[Payment Options] Malformed URL", e);
      throw new PaymentOptionsException(AppErrorCodeEnum.ODP_SEMANTICA, e.getMessage());
    }
  }

  private String getProtocol(Connection connection) {
    ProtocolEnum protocol = connection.getProtocol();

    if (ProtocolEnum.HTTPS.equals(protocol)) {
      return ProtocolEnum.HTTPS.name().toLowerCase();
    }
    if (protocol != null) {
      return protocol.name().toLowerCase();
    }
    return ProtocolEnum.HTTP.name().toLowerCase();
  }
  
  private boolean isEcGpdSpecialGuest(Station station) {
	  if (station.getRestEndpoint() == null || GPD_REST_ENDPOINT.isEmpty()) {
		  return false;
	  }
	  
	  String stationEndpoint = station.getRestEndpoint();
	  // removes possible final "/"
	  while (stationEndpoint.endsWith("/")) {
	      stationEndpoint = stationEndpoint.substring(0, stationEndpoint.length() - 1);
	  }
	 
	  String gpdEndpoint = GPD_REST_ENDPOINT.get();
	  // removes possible final "/"
	  while (gpdEndpoint.endsWith("/")) {
	      gpdEndpoint = gpdEndpoint.substring(0, gpdEndpoint.length() - 1);
	  }
	  
	  return stationEndpoint.equalsIgnoreCase(gpdEndpoint);
  }
}
