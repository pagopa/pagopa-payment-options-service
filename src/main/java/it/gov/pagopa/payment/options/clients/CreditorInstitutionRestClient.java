package it.gov.pagopa.payment.options.clients;

import static it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.clients.gpd.error.OdPErrorResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import it.gov.pagopa.payment.options.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Rest Client for Creditor Institution services */
@ApplicationScoped
public class CreditorInstitutionRestClient {

	private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionRestClient.class);

	private final ObjectMapper objectMapper;

	public CreditorInstitutionRestClient(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	   * Call the creditor institution service to obtain the list paymentOptions related to the input
	   *
	   * @param proxyHost proxy host, optional
	   * @param proxyPort proxy port, optional
	   * @param targetHost verify service host
	   * @param targetPort verify service port
	   * @param targetPath verify service path
	   * @return PaymentOptionResponse
	   * @throws CreditorInstitutionException when an error occurred while processing creditor
	   *     institution response
	   * @throws PaymentOptionsException when an unexpected error occurred
	   */
	  public PaymentOptionsResponse callEcPaymentOptionsVerify(
	      URL baseUrl,
	      String proxyHost,
	      Long proxyPort,
	      String targetHost,
	      Long targetPort,
	      String targetPath) {

	    RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(baseUrl);

	    if (proxyHost != null && proxyPort != null) {
	      builder = builder.proxyAddress(proxyHost, proxyPort.intValue());
	    }
	    CreditorInstitutionRestClientInterface ecRestClientInterface =
	        builder.build(CreditorInstitutionRestClientInterface.class);

	    try {
	      return getPaymentOptions(targetHost, targetPort, targetPath, ecRestClientInterface);
	    } catch (CreditorInstitutionException e) {
	      throw e;
	    } catch (JsonProcessingException e) {
	      Instant now = Instant.now();
	      ErrorResponse errorResponse =
	          buildErrorResponse(
	              CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
	              now.toEpochMilli(),
	              now.truncatedTo(ChronoUnit.MILLIS).toString());
	      throw new CreditorInstitutionException(
	          errorResponse, "[Payment Options] Unable to parse the station response");
	    } catch (Exception e) {
	      logger.error("[Payment Options] Unable to call the station due to error", e);
	      throw new PaymentOptionsException(
	          AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, e.getMessage());
	    }
	  }

	  private PaymentOptionsResponse getPaymentOptions(
	      String targetHost,
	      Long targetPort,
	      String targetPath,
	      CreditorInstitutionRestClientInterface ecRestClientInterface)
	      throws JsonProcessingException {
	    try (Response response =
	        ecRestClientInterface.verifyPaymentOptions(targetHost, targetPort.intValue(), targetPath)) {

	      return this.objectMapper.readValue(
	          response.readEntity(String.class), PaymentOptionsResponse.class);

	    } catch (ClientWebApplicationException e) {
	      logger.error("[Payment Options] Encountered REST client exception", e);
	      Response response = e.getResponse();
	      ErrorResponse errorResponse =
	          this.objectMapper.readValue(response.readEntity(String.class), ErrorResponse.class);
	      errorResponse =
	          validateAndBuildErrorResponse(response.getStatus(), errorResponse, targetPath);

	      throw new CreditorInstitutionException(
	          errorResponse,
	          "[Payment Options] Encountered a managed error calling the station REST endpoint");
	    }
	  }

	  private ErrorResponse validateAndBuildErrorResponse(
	      int responseStatus, ErrorResponse errorResponse, String targetPath) {
	    String responseErrorCode = errorResponse.getAppErrorCode();
	    String errorMessage =
	        String.format(
	            "%s %s %s",
	            ODP_ERRORE_EMESSO_DA_PAA.name(), responseErrorCode, errorResponse.getErrorMessage());
	    ErrorResponse errorResponseForPSP =
	        buildErrorResponse(errorMessage, errorResponse.getTimestamp(), errorResponse.getDateTime());

	    if (CreditorInstitutionErrorEnum.isNotValidErrorCode(responseErrorCode)
	        || CreditorInstitutionErrorEnum.getFromErrorCode(responseErrorCode).getStatus()
	            != responseStatus) {
	      errorResponseForPSP.setErrorMessage(CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name());

	      String orgFiscalCode = extractOrgFiscalCode(targetPath);
	      logger.error(
	          "[Payment Options] [Alert] Organization with fiscal code {} responded with an invalid error code {} response status {} pair",
	          StringUtil.sanitize(orgFiscalCode),
	          responseErrorCode,
	          responseStatus);
	    }
	    return errorResponseForPSP;
	  }

	  private ErrorResponse buildErrorResponse(String errorMessage, Long timestamp, String dateTime) {
	    return ErrorResponse.builder()
	        .httpStatusCode(ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode())
	        .httpStatusDescription(ODP_ERRORE_EMESSO_DA_PAA.getStatus().getReasonPhrase())
	        .appErrorCode(ODP_ERRORE_EMESSO_DA_PAA.getErrorCode())
	        .errorMessage(errorMessage)
	        .timestamp(timestamp)
	        .dateTime(dateTime)
	        .build();
	  }

	  private String extractOrgFiscalCode(String targetPath) {
	    String orgFiscalCode = "";
	    Pattern p = Pattern.compile("/payment-options/organizations/([^/?]+)");
	    Matcher m = p.matcher(targetPath);
	    if (m.find()) {
	      orgFiscalCode = m.group(1);
	    }
	    return orgFiscalCode;
	  }

	  
	  /**
	   * Calls GPD-Core verifyPaymentOptions API (EC GPD "special guest").
	   *
	   * @param gpdBaseEndpoint base endpoint, e.g. https://api.<env>.platform.pagopa.it/gpd/api/v1
	   * @param organizationFiscalCode fiscal code of the organization
	   * @param noticeNumber notice number (NAV)
	   * @param segregationCodes optional segregation codes
	   * @return PaymentOptionsResponse
	   */
	  public PaymentOptionsResponse callGpdPaymentOptionsVerify(
			  String gpdBaseEndpoint,
			  String organizationFiscalCode,
			  String noticeNumber,
			  String segregationCodes
			  ) {

		  final URL baseUrl;
		  try {
			  if (gpdBaseEndpoint == null || gpdBaseEndpoint.isBlank()) {
				  throw new MalformedURLException("GPD-Core endpoint is null or blank");
			  }
			  baseUrl = new URL(gpdBaseEndpoint);
		  } catch (MalformedURLException e) {
			  // Node-side semantics: malformed endpoint
			  throw new PaymentOptionsException(
					  AppErrorCodeEnum.ODP_SEMANTICA,
					  "[Payment Options] Malformed GPD-Core endpoint",
					  e
					  );
		  }

		  // Rest Client
		  RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(baseUrl);
		  GpdCoreRestClientInterface gpdClient = builder.build(GpdCoreRestClientInterface.class);

		  try {
			  // Call to service:
			  //    - if 2xx -> return normal response
			  //    - if 4xx/5xx -> Quarkus throws ClientWebApplicationException
			  try (Response response = gpdClient.verifyPaymentOptions(
					  organizationFiscalCode, noticeNumber, segregationCodes)) {

				  return objectMapper.readValue(
						  response.readEntity(String.class),
						  PaymentOptionsResponse.class
						  );
			  }

		  } catch (ClientWebApplicationException e) {
			  Response response = e.getResponse();
			  if (response != null) {
				  // Business error from GPD-Core: OdPErrorResponse -> ErrorResponse -> CreditorInstitutionException
				  manageGpdErrorResponse(response);
			  }
			  // If there is no response --> network error / handshake / timeout...
			  throw new PaymentOptionsException(
					  AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
					  "[Payment Options] Unable to reach GPD-Core endpoint",
					  e
					  );

		  } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			  // GPD-Core 2xx response parsing error
			  Instant now = Instant.now();
			  ErrorResponse errorResponse =
					  buildErrorResponse(
							  CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
							  now.toEpochMilli(),
							  now.truncatedTo(ChronoUnit.MILLIS).toString()
							  );
			  throw new CreditorInstitutionException(
					  errorResponse,
					  "[Payment Options] Unable to parse the GPD-Core response"
					  );

		  } catch (CreditorInstitutionException e) {
			  // already mapped -> only propagated
			  throw e;

		  } catch (Exception e) {
			  // unexpected errors -> PaymentOptionsException
			  throw new PaymentOptionsException(
					  AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
					  e.getMessage(),
					  e
					  );
		  }
	}

	private void manageGpdErrorResponse(Response response) {
		try {
			// 1) parse GPD-Core error response
			OdPErrorResponse gpdError = objectMapper.readValue(
					response.readEntity(String.class),
					OdPErrorResponse.class
					);

			// 2) map GPD-Core OdPErrorResponse -> Payment Options internal ErrorResponse
			ErrorResponse errorResponse = ErrorResponse.builder()
					.httpStatusCode(gpdError.getHttpStatusCode())
					.httpStatusDescription(gpdError.getHttpStatusDescription())
					.appErrorCode(gpdError.getAppErrorCode())
					.timestamp(gpdError.getTimestamp())
					.dateTime(gpdError.getDateTime())
					.errorMessage(gpdError.getErrorMessage())
					.build();

			// 3) throws as CreditorInstitutionException
			throw new CreditorInstitutionException(
					errorResponse,
					"[Payment Options] Encountered a managed error calling GPD-Core verifyPaymentOptions");

		} catch (CreditorInstitutionException creditorInstitutionException) {
			// the exception is propagated as is
			throw creditorInstitutionException;
		} catch (Exception e) {
			// if GPD error cannot be parsed -> generic fallback
			throw new PaymentOptionsException(
					AppErrorCodeEnum.ODP_SEMANTICA,
					e.getMessage());
		}
	}
	
	

}
