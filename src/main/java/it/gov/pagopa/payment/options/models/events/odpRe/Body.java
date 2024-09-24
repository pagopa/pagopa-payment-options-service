package it.gov.pagopa.payment.options.models.events.odpRe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Body {

  private String insertedTimestamp;
  private String componente;
  private String categoriaEvento;
  private String sottoTipoEvento;
  private String idDominio;
  private String iuv;
  private String ccp;
  private String psp;
  private String tipoVersamento;
  private String tipoEvento;
  private String fruitore;
  private String erogatore;
  private String stazione;
  private String canale;
  private String parametriSpecificiInterfaccia;
  private String esito;
  private String sessionId;
  private String status;
  private String payload;
  private String info;
  private String businessProcess;
  private String fruitoreDescr;
  private String erogatoreDescr;
  private String pspDescr;
  private String noticeNumber;
  private String creditorReferenceId;
  private String paymentToken;
  private String sessionIdOriginal;
  private String standIn;
  private String dataOraEvento;
  private String uniqueId;
  private String version;

}
