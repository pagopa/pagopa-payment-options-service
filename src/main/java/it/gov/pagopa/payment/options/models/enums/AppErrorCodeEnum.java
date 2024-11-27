package it.gov.pagopa.payment.options.models.enums;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;

/**
 * Enumeration for application error codes and messages
 */
@Getter
public enum AppErrorCodeEnum {
    ODP_SINTASSI(Response.Status.BAD_REQUEST, "ODP-001",
        "ODP-SINTASSI. Errore riportato in caso di errore di "
            + "sintassi nel contenuto della richiesta verso ODP"),

    ODP_PSP_SCONOSCIUTO(Response.Status.NOT_FOUND, "ODP-002",
        "ODP_PSP_SCONOSCIUTO. Errore riportato in caso non sia presente fra i "
            + "dati ottenuti da config-cache un psp coincidente con l’id fornito in input"),

    ODP_PSP_DISABILITATO(Response.Status.BAD_REQUEST, "ODP-003",
        "ODP_PSP_DISABILITATO. Errore riportato in caso il psp coincidente con"
            + " l’id fornito in input risulti disabilitato"),

    ODP_INTERMEDIARIO_PSP_SCONOSCIUTO(Status.NOT_FOUND, "ODP-004",
        "ODP_INTERMEDIARIO_PSP_SCONOSCIUTO. Errore riportato in caso il broker "
            + "psp coincidente con i dati forniti in input non sia presente nei "
            + "dati ottenuti da config-cache"),

    ODP_INTERMEDIARIO_PSP_DISABILITATO(Status.BAD_REQUEST, "ODP-005",
        "ODP_INTERMEDIARIO_PSP_DISABILITATO. Errore riportato in caso il broker psp"
            + " coincidente con l’id fornito in input risulti disabilitato"),

    ODP_AUTENTICAZIONE(Status.UNAUTHORIZED, "ODP-006",
        "ODP_AUTENTICAZIONE. Errore di autenticazione rispetto ai dati forniti"),

    ODP_AUTORIZZAZIONE(Status.FORBIDDEN, "ODP-007",
        "ODP_AUTORIZZAZIONE. Errore di autorizzazione rispetto ai dati forniti"),

    ODP_SEMANTICA(Status.BAD_REQUEST, "ODP-008",
        "ODP_SEMANTICA. Errore fornito in caso della presenza di "
            + "casi d’invalidità semantica nei dati del flusso di verifica"),

    ODP_STAZIONE_INT_PA_SCONOSCIUTA(Status.NOT_FOUND, "ODP-009",
        "ODP_STAZIONE_INT_PA_SCONOSCIUTA. "
            + "Errore forninca in caso di assenza associazione stazione e PA"),

    ODP_STAZIONE_INT_PA_DISABILITATA(Status.BAD_REQUEST, "ODP-010",
        "ODP_STAZIONE_INT_PA_DISABILITATA. Errore riportato in caso "
            + "la stazione coincidente con l’id fornito in input risulti disabilitato"),
    ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE(Status.SERVICE_UNAVAILABLE, "ODP-011",
        "ODP_STAZIONE_INT_PA_IRRAGGIUNGIBILE. "
            + "Errore fornito in caso d’irraggiungibilità della stazione"),

    ODP_STAZIONE_INT_PA_SERVIZIO_NON_ATTIVO(Status.BAD_REQUEST, "ODP-012",
        "ODP_STAZIONE_INT_PA_SERVIZIO_NON_ATTIVO. "
            + "Errore fornito in caso di servizio non attivo"),

    ODP_STAZIONE_INT_PA_TIMEOUT(Status.INTERNAL_SERVER_ERROR, "ODP-013",
        "ODP_STAZIONE_INT_PA_TIMEOUT. Errore fornito in caso di timeout della stazione"),

    ODP_ERRORE_EMESSO_DA_PAA(Status.INTERNAL_SERVER_ERROR, "ODP-014",
        "ODP_ERRORE_EMESSO_DA_PAA. Errore fornito in caso di errori da PAA"),

    ODP_STAZIONE_INT_PA_ERRORE_RESPONSE(Status.BAD_REQUEST, "ODP-015",
        "ODP_STAZIONE_INT_PA_ERRORE_RESPONSE. "
            + "Errore fornito in caso di risposta KO dalla stazione"),

    ODP_PSP_NAV_NOT_NMU(Status.BAD_REQUEST, "ODP-016",
        "ODP_PSP_NAV_NOT_NMU. Errore fornito nel caso in cui "
            + "il nav non sia valido per il flusso OdP"),

    ODP_STAZIONE_INT_VERIFICA_ODP_DISABILITATA(Status.BAD_REQUEST, "ODP-017",
        "ODP_SYSTEM_ERROR Codice d’errore generico"),
    ODP_SYSTEM_ERROR(Status.INTERNAL_SERVER_ERROR, "ODP-018",
        "ODP_SYSTEM_ERROR. Codice d’errore generico"),

    ODP_INTERMEDIARIO_PA_DISABILITATO(Status.BAD_REQUEST, "ODP-019",
        "ODP_INTERMEDIARIO_PA_DISABILITATO"),

    ODP_INTERMEDIARIO_PA_SCONOSCIUTO(Status.NOT_FOUND, "ODP-020",
        "ODP_INTERMEDIARIO_PA_SCONOSCIUTO"),

    ODP_DOMINIO_DISABILITATO(Status.BAD_REQUEST, "ODP-021",
        "ODP_DOMINIO_DISABILITATO"),

    ODP_DOMINIO_SCONOSCIUTO(Status.NOT_FOUND, "ODP-022",
        "ODP_DOMINIO_SCONOSCIUTO");

    private final Response.Status status;
    private final String errorCode;
    private final String errorMessage;

    AppErrorCodeEnum(Response.Status status, String errorCode, String errorMessage) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
