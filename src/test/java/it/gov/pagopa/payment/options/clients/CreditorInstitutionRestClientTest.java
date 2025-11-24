package it.gov.pagopa.payment.options.clients;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.exception.CreditorInstitutionException;
import it.gov.pagopa.payment.options.exception.PaymentOptionsException;
import it.gov.pagopa.payment.options.models.clients.creditorInstitution.PaymentOptionsResponse;
import it.gov.pagopa.payment.options.models.clients.gpd.error.OdPErrorResponse;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import it.gov.pagopa.payment.options.models.enums.CreditorInstitutionErrorEnum;
import it.gov.pagopa.payment.options.services.CreditorInstitutionService;
import it.gov.pagopa.payment.options.test.extensions.WireMockExtensions;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.databind.ObjectMapper;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class CreditorInstitutionRestClientTest {

  private static final String TARGET_HOST = "http://externalService";
  private static final long TARGET_PORT = 443L;

  @ConfigProperty(name = "CreditorInstitutionRestClient.apimEndpoint")
  private String wiremockUrl;

  @ConfigProperty(name = "wiremock.port")
  private String wiremockPort;

  @Inject CreditorInstitutionRestClient creditorInstitutionRestClient;
  
  @Inject CreditorInstitutionService sut;

  @Test
  void callEcPaymentOptionsVerifyShouldReturnData() {
    PaymentOptionsResponse paymentOptionsResponse =
        assertDoesNotThrow(
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    new URL(wiremockUrl),
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/77777777777/notices/311111111112222222"));

    assertNotNull(paymentOptionsResponse);
  }

  @ParameterizedTest
  @ValueSource(strings = {"87777777777", "57777777777", "97777777777"})
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnErrorResponseWithFailureOnResponseParseOrValidation(
      String fiscalCode) {
    URL url = new URL(wiremockUrl);
    String targetPath =
        String.format("/payment-options/organizations/%s/notices/311111111112222222", fiscalCode);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url, null, null, TARGET_HOST, TARGET_PORT, targetPath));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode(),
        exception.getErrorResponse().getHttpStatusCode());
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
    assertEquals(
        CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
        exception.getErrorResponse().getErrorMessage());
  }

  @Test
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnErrorResponse() {
    URL url = new URL(wiremockUrl);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url,
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/67777777777/notices/311111111112222222"));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getStatus().getStatusCode(),
        exception.getErrorResponse().getHttpStatusCode());
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
    assertTrue(
        exception
            .getErrorResponse()
            .getErrorMessage()
            .startsWith(AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.name()));
  }

  @Test
  @SneakyThrows
  void callEcPaymentOptionsVerifyShouldReturnUnreachableKOWithoutErrorResponse() {
    URL url = new URL(wiremockUrl);

    CreditorInstitutionException exception =
        assertThrows(
            CreditorInstitutionException.class,
            () ->
                creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                    url,
                    null,
                    null,
                    TARGET_HOST,
                    TARGET_PORT,
                    "/payment-options/organizations/08888888888/notices/88888888888"));

    assertNotNull(exception);
    assertEquals(
        AppErrorCodeEnum.ODP_ERRORE_EMESSO_DA_PAA.getErrorCode(),
        exception.getErrorResponse().getAppErrorCode());
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnMalformedUrl() {
    assertThrows(
        MalformedURLException.class,
        () ->
            creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                new URL("AAAAAAA"),
                null,
                null,
                TARGET_HOST,
                TARGET_PORT,
                "/payment-options/organizations/08888888888/notices/88888888888"));
  }

  @Test
  void callEcPaymentOptionsVerifyShouldReturnExceptionOnWrongProxy() {
    assertThrows(
        Exception.class,
        () ->
            creditorInstitutionRestClient.callEcPaymentOptionsVerify(
                new URL("AAAAAAA"),
                "AAAAAAA%%%",
                8081L,
                TARGET_HOST,
                TARGET_PORT,
                "/payment-options/organizations/08888888888/notices/88888888888"));
  }
  
 //============================================================
 //  GPD-Core "special guest"
 // ============================================================
  
  @Test
  void callGpdPaymentOptionsVerifyShouldReturnDataOn2xx() throws Exception {
      String gpdEndpoint = "http://localhost:8080";

      // mock static per RestClientBuilder
      try (MockedStatic<RestClientBuilder> builderStatic = mockStatic(RestClientBuilder.class)) {
          RestClientBuilder builder = mock(RestClientBuilder.class);
          GpdCoreRestClientInterface gpdClient = mock(GpdCoreRestClientInterface.class);
          Response response = mock(Response.class);

          builderStatic.when(RestClientBuilder::newBuilder).thenReturn(builder);
          when(builder.baseUrl(any(URL.class))).thenReturn(builder);
          when(builder.build(GpdCoreRestClientInterface.class)).thenReturn(gpdClient);

          PaymentOptionsResponse expected = PaymentOptionsResponse.builder().build();
          String body = new ObjectMapper().writeValueAsString(expected);
          when(gpdClient.verifyPaymentOptions(any(), any(), any())).thenReturn(response);
          when(response.readEntity(String.class)).thenReturn(body);

          PaymentOptionsResponse result = creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
              gpdEndpoint,
              "77777777777",
              "311111111111111111",
              null
          );

          assertNotNull(result);
      }
  }
  
  @Test
  void callGpdPaymentOptionsVerifyShouldThrowOnMalformedEndpoint() {
      PaymentOptionsException ex = assertThrows(
          PaymentOptionsException.class,
          () -> creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
              "  ",         
              "77777777777",
              "311111111111111111",
              null
          )
      );

      assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
      assertTrue(ex.getMessage().contains("Malformed GPD-Core endpoint"));
  } 

 @Test
 void callGpdPaymentOptionsVerifyShouldThrowSemanticaOnNullEndpoint() {
   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                   null, "77777777777", "311111111112222222", null));

   assertNotNull(ex);
   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
   assertTrue(ex.getMessage().contains("Malformed GPD-Core endpoint"));
 }
 
 @Test
 void callGpdPaymentOptionsVerifyShouldMapBusinessErrorFromGpd() throws Exception {
     String gpdEndpoint = "http://localhost:8080";

     try (MockedStatic<RestClientBuilder> builderStatic = mockStatic(RestClientBuilder.class)) {
         RestClientBuilder builder = mock(RestClientBuilder.class);
         GpdCoreRestClientInterface gpdClient = mock(GpdCoreRestClientInterface.class);
         Response response = mock(Response.class);

         builderStatic.when(RestClientBuilder::newBuilder).thenReturn(builder);
         when(builder.baseUrl(any(URL.class))).thenReturn(builder);
         when(builder.build(GpdCoreRestClientInterface.class)).thenReturn(gpdClient);

         OdPErrorResponse gpdError = OdPErrorResponse.builder()
             .httpStatusCode(404)
             .httpStatusDescription("Not Found")
             .appErrorCode("ODP-404")
             .timestamp(1724425035L)
             .dateTime("2024-08-23T14:57:15.635528")
             .errorMessage("PAA_SOME_ERROR details")
             .build();
         String body = new ObjectMapper().writeValueAsString(gpdError);

         when(response.readEntity(String.class)).thenReturn(body);
         when(response.getStatus()).thenReturn(404);

         ClientWebApplicationException cwae =
             new ClientWebApplicationException("404 from GPD", response);
         when(gpdClient.verifyPaymentOptions(any(), any(), any())).thenThrow(cwae);

         CreditorInstitutionException ex = assertThrows(
             CreditorInstitutionException.class,
             () -> creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                 gpdEndpoint,
                 "77777777777",
                 "311111111111111111",
                 null
             )
         );

         assertNotNull(ex.getErrorResponse());
         assertEquals(404, ex.getErrorResponse().getHttpStatusCode());
         assertEquals("ODP-404", ex.getErrorResponse().getAppErrorCode());
     }
 }
 
 @Test
 void callGpdPaymentOptionsVerifyShouldMapNetworkErrorToPaymentOptionsException() {
     String gpdEndpoint = "http://localhost:8080";

     try (MockedStatic<RestClientBuilder> builderStatic = mockStatic(RestClientBuilder.class)) {
         RestClientBuilder builder = mock(RestClientBuilder.class);
         GpdCoreRestClientInterface gpdClient = mock(GpdCoreRestClientInterface.class);

         builderStatic.when(RestClientBuilder::newBuilder).thenReturn(builder);
         when(builder.baseUrl(any(URL.class))).thenReturn(builder);
         when(builder.build(GpdCoreRestClientInterface.class)).thenReturn(gpdClient);

         ClientWebApplicationException cwae = mock(ClientWebApplicationException.class);
         when(cwae.getResponse()).thenReturn(null);
         when(gpdClient.verifyPaymentOptions(any(), any(), any())).thenThrow(cwae);

         PaymentOptionsException ex = assertThrows(
             PaymentOptionsException.class,
             () -> creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                 gpdEndpoint,
                 "77777777777",
                 "311111111111111111",
                 null
             )
         );

         assertEquals(AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE, ex.getErrorCode());
     }
 }

 
 @Test
 void callGpdPaymentOptionsVerifyShouldMapParsingErrorToCreditorInstitutionException() throws Exception {
     String gpdEndpoint = "http://localhost:8080";

     try (MockedStatic<RestClientBuilder> builderStatic = mockStatic(RestClientBuilder.class)) {
         RestClientBuilder builder = mock(RestClientBuilder.class);
         GpdCoreRestClientInterface gpdClient = mock(GpdCoreRestClientInterface.class);
         Response response = mock(Response.class);

         builderStatic.when(RestClientBuilder::newBuilder).thenReturn(builder);
         when(builder.baseUrl(any(URL.class))).thenReturn(builder);
         when(builder.build(GpdCoreRestClientInterface.class)).thenReturn(gpdClient);

         when(gpdClient.verifyPaymentOptions(any(), any(), any())).thenReturn(response);
         when(response.readEntity(String.class)).thenReturn("this-is-not-json");

         CreditorInstitutionException ex = assertThrows(
             CreditorInstitutionException.class,
             () -> creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                 gpdEndpoint,
                 "77777777777",
                 "311111111111111111",
                 null
             )
         );

         assertEquals(
             CreditorInstitutionErrorEnum.PAA_SYSTEM_ERROR.name(),
             ex.getErrorResponse().getErrorMessage()
         );
     }
 }
 
 @Test
 void manageGpdErrorResponseShouldThrowCreditorInstitutionException() throws Exception {
     OdPErrorResponse gpdError =
         OdPErrorResponse.builder()
             .httpStatusCode(404)
             .httpStatusDescription("Not Found")
             .appErrorCode("ODP-404")
             .timestamp(1724425035L)
             .dateTime("2024-08-23T14:57:15.635528")
             .errorMessage("PAA_SOME_ERROR details")
             .build();

     String json = new ObjectMapper().writeValueAsString(gpdError);

     Response response = mock(Response.class);
     when(response.readEntity(String.class)).thenReturn(json);

     CreditorInstitutionRestClient localClient =
         new CreditorInstitutionRestClient(new ObjectMapper());

     RuntimeException thrown = assertThrows(
         RuntimeException.class,
         () -> invokeManageGpdErrorResponse(response, localClient)
     );

     assertTrue(thrown instanceof CreditorInstitutionException);
 }

 
 @Test
 void manageGpdErrorResponseShouldThrowPaymentOptionsExceptionOnInvalidJson() throws Exception {
     Response response = mock(Response.class);
     when(response.readEntity(String.class)).thenReturn("not-a-json");

     Throwable thrown = assertThrows(
         Throwable.class,
         () -> invokeManageGpdErrorResponse(response, creditorInstitutionRestClient)
     );

     Throwable cause = (thrown.getCause() instanceof PaymentOptionsException)
         ? thrown.getCause()
         : thrown;

     assertTrue(cause instanceof PaymentOptionsException);
     assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ((PaymentOptionsException) cause).getErrorCode());
 }



/*

 @ParameterizedTest
 @ValueSource(strings = {"", "   ", "not-a-valid-url"})
 void callGpdPaymentOptionsVerifyShouldThrowSemanticaOnInvalidEndpoint(String gpdEndpoint) {
   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               creditorInstitutionRestClient.callGpdPaymentOptionsVerify(
                   gpdEndpoint, "77777777777", "311111111112222222", null));

   assertNotNull(ex);
   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
 }

 @Test
 void callGpdPaymentOptionsVerifyShouldPropagatePaymentOptionsException() {
   PaymentOptionsException clientException =
       new PaymentOptionsException(
           AppErrorCodeEnum.ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE,
           "[Payment Options] Unable to reach GPD-Core endpoint");

   CreditorInstitutionRestClient spyClient =
       Mockito.spy(creditorInstitutionRestClient);

   Mockito.doThrow(clientException)
       .when(spyClient)
       .callGpdPaymentOptionsVerify(any(), any(), any(), any());

   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () ->
               spyClient.callGpdPaymentOptionsVerify(
                   "http://dummy-gpd-endpoint", "77777777777", "311111111112222222", null));

   assertSame(clientException, ex);
   assertSame(clientException.getErrorCode(), ex.getErrorCode());
 }
 
 @Test
 void manageGpdErrorResponseShouldThrowCreditorInstitutionException() throws Exception {
   OdPErrorResponse gpdError =
       OdPErrorResponse.builder()
           .httpStatusCode(404)
           .httpStatusDescription("Not Found")
           .appErrorCode("ODP-404")
           .timestamp(1724425035L)
           .dateTime("2024-08-23T14:57:15.635528")
           .errorMessage("PAA_SOME_ERROR some details")
           .build();

   String json = new ObjectMapper().writeValueAsString(gpdError);

   Response response = mock(Response.class);
   when(response.readEntity(String.class)).thenReturn(json);
   
   // the private method is invoked by reflection
   PaymentOptionsException ex =
		      assertThrows(
		          PaymentOptionsException.class,
		          () -> invokeManageGpdErrorResponse(response, creditorInstitutionRestClient));
   
   assertNotNull(ex);
   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
 }
 
 @Test
 void manageGpdErrorResponseShouldThrowPaymentOptionsExceptionOnInvalidBody() throws Exception {
   // entity NOT parsable as OdPErrorResponse
   Response response = Response.status(500).entity("this is not json").build();

   PaymentOptionsException ex =
       assertThrows(
           PaymentOptionsException.class,
           () -> invokeManageGpdErrorResponse(response, creditorInstitutionRestClient));

   assertEquals(AppErrorCodeEnum.ODP_SEMANTICA, ex.getErrorCode());
 }*/
 
 private RuntimeException invokeManageGpdErrorResponse(
		 Response response, CreditorInstitutionRestClient client) throws Exception {
	 Method m =
			 CreditorInstitutionRestClient.class.getDeclaredMethod(
					 "manageGpdErrorResponse", Response.class);
	 m.setAccessible(true);
	 try {
		 m.invoke(client, response);
		 return null;
	 } catch (InvocationTargetException ite) {
		 Throwable cause = ite.getCause();
		 if (cause instanceof RuntimeException re) {
			 throw re;
		 } else {
			 throw new RuntimeException(cause);
		 }
	 }
 }
 
  
}