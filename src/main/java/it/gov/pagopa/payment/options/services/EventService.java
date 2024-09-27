package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.models.events.odpRe.Body;
import it.gov.pagopa.payment.options.models.events.odpRe.EventType;
import it.gov.pagopa.payment.options.models.events.odpRe.OdpVerifyRe;
import it.gov.pagopa.payment.options.models.events.odpRe.Properties;
import it.gov.pagopa.payment.options.models.events.odpRe.Status;
import it.gov.pagopa.payment.options.models.events.verifyKo.Creditor;
import it.gov.pagopa.payment.options.models.events.verifyKo.DebtorPosition;
import it.gov.pagopa.payment.options.models.events.verifyKo.FaultBean;
import it.gov.pagopa.payment.options.models.events.verifyKo.Psp;
import it.gov.pagopa.payment.options.models.events.verifyKo.VerifyEventKo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service managing sending of events for Payment Options
 */
@ApplicationScoped
public class EventService {

  private final Logger logger = LoggerFactory.getLogger(EventService.class);

  private String SERVICE_IDENTIFIER = "ODP";

  @Inject
  @Channel("nodo-dei-pagamenti-verify-ko")
  Emitter<VerifyEventKo> verifyKoEmitter;

  @Inject
  @Channel("opzioni-di-pagamento-re")
  Emitter<OdpVerifyRe> odpVerifyReEmitter;

  /**
   * Sending an event to the verify-ko event
   * @param idPsp input PSP
   * @param idBrokerPsp input Broker PSP
   * @param noticeNumber input Notice Number
   * @param fiscalCode input Fiscal Code
   * @param idStation extracted station code
   * @param idBrokerPA extracted brokerPA
   * @param appErrorCode error code to notify
   * @param errorMessage error code to notify
   * @param timestamp event timestamp
   * @param dateTime event dateTime
   */
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
              .serviceIdentifier(SERVICE_IDENTIFIER)
              .build()
      );
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event to ko topic: {}",
          e.getMessage());
    }

  }

  /**
   * Produces an event to the payment-options-re regarding the PSP interface
   * @param idPsp input PSP
   * @param noticeNumber input notice number
   * @param fiscalCode input fiscalCode
   * @param idStation extracted idStation
   * @param sessionId sessionId to trace events
   * @param dateTime event date-time
   * @param esito event outcome
   * @param eventType outcome eventType
   * @param payload payload for the event (if existing)
   */
  public void sendEvent(
      /* Input Request **/
      String idPsp, String idBrokerPsp,
      String noticeNumber, String fiscalCode,
      /* Extracted data */
      String idStation,
      /* Meta content */
      String sessionId, String dateTime,
      Status esito, EventType eventType,
      /* Response Content */
      String payload, String errorCode, String errorDescription

  ) {
    try {
      odpVerifyReEmitter.send(
          OdpVerifyRe.builder()
              .body(
                  Body.builder()
                  .id(UUID.randomUUID().toString())
                  .sessionId(sessionId)
                  .insertedTimestamp(dateTime)
                  .eventTimestamp(dateTime)
                  .status(esito)
                  .eventType(eventType)
                  .stationId(idStation)
                  .sessionId(sessionId)
                  .pspId(idPsp)
                  .brokerId(idBrokerPsp)
                  .organizationId(fiscalCode)
                  .noticeNumber(noticeNumber)
                  .errorStatusCode(errorCode)
                  .errorStatusDesc(errorDescription)
                  .payload(payload != null ?
                      Base64.getMimeEncoder().encodeToString(payload.getBytes()) : null)
                  .version("1")
                .build()
              )
              .properties(Properties.builder()
                  .serviceIdentifier(SERVICE_IDENTIFIER)
                  .build())
              .build()
      );
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event for Odp res topic: {}",
          e.getMessage());
    }
  }

}
