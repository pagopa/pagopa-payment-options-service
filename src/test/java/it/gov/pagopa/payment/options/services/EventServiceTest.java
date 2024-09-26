package it.gov.pagopa.payment.options.services;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.models.events.odpRe.EventType;
import it.gov.pagopa.payment.options.test.extensions.KafkaTestResourceLifecycleManager;
import it.gov.pagopa.payment.options.models.events.odpRe.Status;
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

  @Test
  void sendOdpRePspEvent() {
    assertDoesNotThrow(() -> eventService.sendEvent(
        "0001", "0001", "0001",
        "0000001", "00001", null,
        "12131313", Status.OK, EventType.RES, null,
        null, null
    ));
  }

  @Test
  void sendOdpReECEvent() {
    assertDoesNotThrow(() -> eventService.sendEvent(
        "0001", "0001", "0001",
        "0000001", "00001", null,
        "12131313", Status.KO, EventType.REQ, null,
        null, null
    ));
  }



}
