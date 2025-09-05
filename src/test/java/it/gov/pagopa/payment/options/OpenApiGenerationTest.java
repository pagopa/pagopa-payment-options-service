package it.gov.pagopa.payment.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.models.enums.AppErrorCodeEnum;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

@QuarkusTest
class OpenApiGenerationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void swaggerSpringPlugin() throws Exception {

        String responseString =
                given()
                        .when().get("/openapi.json")
                        .then()
                        .statusCode(200)
                        .contentType("application/json")
                        .extract()
                        .asString();

        Object swagger = objectMapper.readValue(responseString, Object.class);
        String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
        formatted = formatted.replace("placeholder-for-replace", getAppErrorCodes());
        Path basePath = Paths.get("openapi/");
        Files.createDirectories(basePath);
        Files.write(basePath.resolve("openapiForPsp.json"), formatted.getBytes());
    }

    private String getAppErrorCodes() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("PagoPa Payment Options service \\n ### APP ERROR CODES ### \\n\\n\\n <details><summary>Details</summary>\\n **NAME** | **HTTP STATUS CODE** | **DESCRIPTION** \\n- | - | - ");
        for (AppErrorCodeEnum errorCode : AppErrorCodeEnum.values()) {
            stringBuilder
                    .append("\\n **")
                    .append(errorCode.getErrorCode())
                    .append("** | *")
                    .append(errorCode.getStatus())
                    .append("* | ")
                    .append(errorCode.getErrorMessage());
        }
        stringBuilder.append(" \\n\\n </details> \\n");
        return stringBuilder.toString();
    }
}
