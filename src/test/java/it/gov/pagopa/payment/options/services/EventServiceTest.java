package it.gov.pagopa.payment.options.services;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.KafkaTestResourceLifecycleManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
class EventServiceTest {

  @Inject
  public EventService eventService;

  @Test
  void sendVerifyKoEvent() {
    assertDoesNotThrow(() -> eventService.sendVerifyKoEvent(
        "0001", "0001", "0000001", "00001", "00001",
        "00001", "ODP-001", "Error",
        Instant.now().getEpochSecond(), null
    ));
  }


}
