package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.models.events.odpRe.OdpVerifyRe;
import it.gov.pagopa.payment.options.models.events.odpRe.Properties;
import it.gov.pagopa.payment.options.models.events.verifyKo.Creditor;
import it.gov.pagopa.payment.options.models.events.verifyKo.DebtorPosition;
import it.gov.pagopa.payment.options.models.events.verifyKo.FaultBean;
import it.gov.pagopa.payment.options.models.events.verifyKo.Psp;
import it.gov.pagopa.payment.options.models.events.verifyKo.VerifyEventKo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@ApplicationScoped
public class EventService {

  private final Logger logger = LoggerFactory.getLogger(EventService.class);

  @ConfigProperty(name = "payment-options.event.serviceIdentifier")
  String serviceIdentifier;

  @Inject
  @Channel("nodo-dei-pagamenti-cache")
  Emitter<VerifyEventKo> verifyKoEmitter;

  @Inject
  @Channel("opzioni-di-pagamento-re")
  Emitter<OdpVerifyRe> odpVerifyReEmitter;

  public void sendVerifyKoEvent(
      /* Input Request **/
      String idPsp, String idBrokerPsp, String noticeNumber, String fiscalCode,
      /* Extracted data */
      String idStation, String idBrokerPA,
      /* Error content */
      String appErrorCode, String errorMessage, Long timestamp, String dateTime) {

    try {
      verifyKoEmitter.send(
          VerifyEventKo.builder()
              .id(UUID.randomUUID().toString())
              .psp(Psp.builder().idPsp(idPsp).idBrokerPsp(idBrokerPsp).build())
              .debtorPosition(DebtorPosition.builder().noticeNumber(noticeNumber).build())
              .creditor(
                  Creditor.builder().idPA(fiscalCode).idStation(idStation).idBrokerPA(idBrokerPA)
                      .build()
              )
              .faultBean(
                  FaultBean.builder()
                      .faultCode(appErrorCode)
                      .description(errorMessage)
                      .timestamp(timestamp)
                      .dateTime(dateTime)
                      .build()
              )
              .serviceIdentifier(serviceIdentifier)
              .build()
      );
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event to ko topic: {}",
          e.getMessage());
    }

  }

  public void sendOdpReEvent() {
    try {
      odpVerifyReEmitter.send(
          OdpVerifyRe.builder()
              .properties(Properties.builder()
                  .serviceIdentifier(serviceIdentifier)
                  .build())
              .build());
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event for Odp res topic: {}",
          e.getMessage());
    }
  }

}
