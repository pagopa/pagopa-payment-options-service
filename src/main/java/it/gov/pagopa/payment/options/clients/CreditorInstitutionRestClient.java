package it.gov.pagopa.payment.options.clients;

import static it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.ErrorResponse;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import it.gov.pagopa.payment.options.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Rest Client for Creditor Institution services */
@ApplicationScoped
public class CreditorInstitutionRestClient {
	
	private final GpdCoreRestClientInterface gpdClient;

	private final Logger logger = LoggerFactory.getLogger(CreditorInstitutionRestClient.class);

	private final ObjectMapper objectMapper;

	@Inject
	public CreditorInstitutionRestClient(ObjectMapper objectMapper, 
			@RestClient GpdCoreRestClientInterface gpdClient) {
		this.objectMapper = objectMapper;
		this.gpdClient = gpdClient;
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
	          validateAndBuildErrorResponse(response.getStatus(), errorResponse, extractOrgFiscalCode(targetPath));

	      throw new CreditorInstitutionException(
	          errorResponse,
	          "[Payment Options] Encountered a managed error calling the station REST endpoint");
	    }
	  }

	  private ErrorResponse validateAndBuildErrorResponse(
	      int responseStatus, ErrorResponse errorResponse, String orgFiscalCode) {
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
	   * @param organizationFiscalCode fiscal code of the organization
	   * @param noticeNumber notice number (NAV)
	   * @param segregationCodes optional segregation codes
	   * @return PaymentOptionsResponse
	   */
	  public PaymentOptionsResponse callGpdPaymentOptionsVerify(
			  String organizationFiscalCode,
			  String noticeNumber,
			  String segregationCodes
			  ) {
		  try (Response response = gpdClient.verifyPaymentOptions(
				  organizationFiscalCode, noticeNumber, segregationCodes)) {

			  return objectMapper.readValue(
					  response.readEntity(String.class),
					  PaymentOptionsResponse.class
					  );

		  } catch (ClientWebApplicationException e) {

			  throw handleClientWebApplicationException(e, organizationFiscalCode);

		  } catch (com.fasterxml.jackson.core.JsonProcessingException e) {

			  throw handleJsonProcessingException();

		  } catch (CreditorInstitutionException e) {
			  throw e;

		  } catch (Exception e) {
			  throw new PaymentOptionsException(
					  AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
					  e.getMessage(),
					  e
					  );
		  }
	  }

	  
	  private RuntimeException handleClientWebApplicationException(
			  ClientWebApplicationException e,
			  String organizationFiscalCode
			  ) {

		  Response resp = e.getResponse();

		  if (resp != null) {
			  // business error from GPD-Core
			  try {
				  ErrorResponse errorResponse = objectMapper.readValue(
						  resp.readEntity(String.class),
						  ErrorResponse.class
						  );

				  errorResponse = validateAndBuildErrorResponse(
						  resp.getStatus(),
						  errorResponse,
						  organizationFiscalCode
						  );

				  return new CreditorInstitutionException(
						  errorResponse,
						  "[Payment Options] Encountered a managed error calling GPD-Core verifyPaymentOptions"
						  );

			  } catch (CreditorInstitutionException ex) {
				  return ex;

			  } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
				  return handleJsonProcessingException();

			  } catch (Exception ex) {
				  return new PaymentOptionsException(
						  AppErrorCodeEnum.ODP_SEMANTICA,
						  ex.getMessage(),
						  ex
						  );
			  }
		  }

		  // No response → networking error
		  return new PaymentOptionsException(
				  AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
				  "[Payment Options] Unable to reach GPD-Core endpoint",
				  e
				  );
	  }


	  private CreditorInstitutionException handleJsonProcessingException() {
		  Instant now = Instant.now();
		  ErrorResponse fallback = buildErrorResponse(
				  CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
				  now.toEpochMilli(),
				  now.truncatedTo(ChronoUnit.MILLIS).toString()
				  );

		  return new CreditorInstitutionException(
				  fallback,
				  "[Payment Options] Unable to parse the GPD-Core response"
				  );
	  }
}
