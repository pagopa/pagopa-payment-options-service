package it.gov.pagopa.payment.options.models.events.odpRe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Base64;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OdpVerifyRe {

  private String uniqueId;
  private String version;
  private String componente;
  private String serviceIdentifier;
  private String businessProcess;
  private String erogatore;
  private CategoriaEvento categoriaEvento;
  private SottoTipoEvento sottoTipoEvento;
  private String sessionId;
  private String psp;
  private String brokerPsp;
  private String fruitore;
  private String idDominio;
  private String stazione;
  private String iuv;
  private String noticeNumber;
  private Esito esito;
  private String payload;
  private String tipoEvento;
  private String erogatoreDescr;
  private String dataOraEvento;
  private String insertedTimestamp;

}
