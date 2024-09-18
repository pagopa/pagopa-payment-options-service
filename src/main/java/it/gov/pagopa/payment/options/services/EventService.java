package it.gov.pagopa.payment.options.services;

import it.gov.pagopa.payment.options.models.events.odpRe.CategoriaEvento;
import it.gov.pagopa.payment.options.models.events.odpRe.Esito;
import it.gov.pagopa.payment.options.models.events.odpRe.OdpVerifyRe;
import it.gov.pagopa.payment.options.models.events.odpRe.SottoTipoEvento;
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

@ApplicationScoped
public class EventService {

  private final Logger logger = LoggerFactory.getLogger(EventService.class);

  private String SERVICE_IDENTIFIER = "ODP_SERV_001";
  private String EROGATORE = "PaymentOptionsService";

  private String PSP_BUSINESS_PROCESS = "verifyOdpPaymentOptions";

  private String COMPONENTE = "FESP";

  @Inject
  @Channel("nodo-dei-pagamenti-verify-ko")
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
              .serviceIdentifier(SERVICE_IDENTIFIER)
              .build()
      );
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event to ko topic: {}",
          e.getMessage());
    }

  }

  public void sendOdpRePspEvent(
      /* Input Request **/
      String idPsp, String noticeNumber, String fiscalCode,
      /* Extracted data */
      String idStation,
      /* Meta content */
      String sessionId, String dateTime,
      Esito esito, SottoTipoEvento sottoTipoEvento,
      /* Response Content */
      String payload

  ) {
    try {
      odpVerifyReEmitter.send(
          OdpVerifyRe.builder()
               .uniqueId(UUID.randomUUID().toString())
               .insertedTimestamp(dateTime)
               .dataOraEvento(dateTime)
               .businessProcess(PSP_BUSINESS_PROCESS)
               .tipoEvento(PSP_BUSINESS_PROCESS)
               .componente(COMPONENTE)
               .categoriaEvento(CategoriaEvento.INTERFACCIA)
               .sottoTipoEvento(sottoTipoEvento)
               .esito(esito)
               .serviceIdentifier(SERVICE_IDENTIFIER)
               .erogatore(EROGATORE)
               .erogatoreDescr(EROGATORE)
               .businessProcess(PSP_BUSINESS_PROCESS)
               .sessionId(sessionId)
               .psp(idPsp)
               .idDominio(fiscalCode)
               .noticeNumber(noticeNumber)
               .stazione(idStation)
               .payload(payload != null ?
                   Base64.getMimeEncoder().encodeToString(payload.getBytes()) : null)
          .build());
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event for Odp res topic: {}",
          e.getMessage());
    }
  }

  public void sendOdpReEcEvent(
      /* Input Request **/
      String idPsp, String noticeNumber, String fiscalCode,
      /* Extracted data */
      String idStation,
      /* Meta content */
      String sessionId, String dateTime,
      Esito esito, SottoTipoEvento sottoTipoEvento,
      /* Response Content */
      String payload

  ) {
    try {
      odpVerifyReEmitter.send(
          OdpVerifyRe.builder()
              .uniqueId(UUID.randomUUID().toString())
              .sessionId(sessionId)
              .insertedTimestamp(dateTime)
              .dataOraEvento(dateTime)
              .businessProcess(PSP_BUSINESS_PROCESS)
              .tipoEvento(PSP_BUSINESS_PROCESS)
              .componente(COMPONENTE)
              .categoriaEvento(CategoriaEvento.INTERFACCIA)
              .sottoTipoEvento(sottoTipoEvento)
              .esito(esito)
              .serviceIdentifier(SERVICE_IDENTIFIER)
              .erogatore(idStation)
              .erogatoreDescr(idStation)
              .businessProcess(PSP_BUSINESS_PROCESS)
              .sessionId(sessionId)
              .psp(idPsp)
              .idDominio(fiscalCode)
              .noticeNumber(noticeNumber)
              .stazione(idStation)
              .payload(payload != null ?
                  Base64.getMimeEncoder().encodeToString(payload.getBytes()) : null)
              .build());
    } catch (Exception e) {
      logger.error(
          "[Payment Options] error encountered while sending event for Odp res topic: {}",
          e.getMessage());
    }
  }

}