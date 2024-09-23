package it.gov.pagopa.payment.options.clients;

import it.gov.pagopa.payment.options.clients.model.ConfigDataV1;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

/**
 * Client for the api-config-cache services
 */
@Path("/cache")
@RegisterRestClient
public interface ApiConfigCacheClient {

  /**
   * Retrieve cache from the provided service
   * @param keys list of strings to be used as filter for provided data
   * @return required cache data
   */
  @GET
  @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${ApiConfigCacheClient.ocpSubKey}")
  ConfigDataV1 getCache(@QueryParam("keys") List<String> keys);

}
