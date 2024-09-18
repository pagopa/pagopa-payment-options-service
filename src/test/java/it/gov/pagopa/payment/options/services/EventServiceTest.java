package it.gov.pagopa.payment.options.services;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.payment.options.KafkaTestResourceLifecycleManager;
import it.gov.pagopa.payment.options.models.events.odpRe.Esito;
import it.gov.pagopa.payment.options.models.events.odpRe.SottoTipoEvento;
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
    assertDoesNotThrow(() -> eventService.sendOdpRePspEvent(
        "0001", "0001", "0000001", "00001", "00001",
        null, Esito.RICEVUTA, SottoTipoEvento.RES, null
    ));
  }

  @Test
  void sendOdpReECEvent() {
    assertDoesNotThrow(() -> eventService.sendOdpRePspEvent(
        "0001", "0001", "0000001", "00001", "00001",
        null, Esito.RICEVUTA, SottoTipoEvento.REQ, null
    ));
  }



}
