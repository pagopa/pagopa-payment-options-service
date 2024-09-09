package it.gov.pagopa.payment.options.clients;

import it.gov.pagopa.payment.options.clients.model.ConfigDataV1;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@Path("/cache")
@RegisterRestClient
public interface ApiConfigCacheClient {

  @GET
  ConfigDataV1 getCache(@QueryParam("keys") List<String> keys);

}
