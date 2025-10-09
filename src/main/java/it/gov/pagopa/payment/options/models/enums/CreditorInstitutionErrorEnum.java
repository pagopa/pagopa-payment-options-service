package it.gov.pagopa.payment.options.models.enums;

import io.undertow.httpcore.StatusCodes;
import lombok.Getter;

import java.util.Arrays;

/**
 * Enumeration for creditor institution response error codes and messages
 */
@Getter
public enum CreditorInstitutionErrorEnum {
    PAA_SINTASSI(StatusCodes.BAD_REQUEST, "ODP-101", "Errore di sintassi del modello nella richiesta"),
    PAA_SEMANTICA(StatusCodes.UNPROCESSABLE_ENTITY, "ODP-102", "Errore fornito in caso della presenza di casi d’invalidità semantica nei dati del flusso di verifica"),
    PAA_SYSTEM_ERROR(StatusCodes.INTERNAL_SERVER_ERROR, "ODP-103", "Errore generico"),
    PAA_ID_DOMINIO_ERRATO(StatusCodes.BAD_REQUEST, "ODP-104", "Errore fornito nel caso di dominio errato fornito"),
    PAA_ID_INTERMEDIARIO_ERRATO(StatusCodes.BAD_REQUEST, "ODP-105", "Errore nel caso sia fornito un intermediario errato"),
    PAA_STAZIONE_INT_ERRATA(StatusCodes.BAD_REQUEST, "ODP-106", "Errore nel caso sia fornita una stazione errata"),
    PAA_PAGAMENTO_SCONOSCIUTO(StatusCodes.NOT_FOUND, "ODP-107", "Errore per pagamento sconosciuto"),
    PAA_PAGAMENTO_DUPLICATO(StatusCodes.CONFLICT, "ODP-108", "Errore per pagamento duplicato"),
    PAA_PAGAMENTO_IN_CORSO(StatusCodes.CONFLICT, "ODP-109", "Errore per pagamento ancora in corso"),
    PAA_PAGAMENTO_SCADUTO(StatusCodes.UNPROCESSABLE_ENTITY, "ODP-110", "Errore per pagamento ancora scaduto"),
    PAA_PAGAMENTO_ANNULLATO(StatusCodes.UNPROCESSABLE_ENTITY, "ODP-111", "Errore per pagamento annullato");

    private final int status;
    private final String errorCode;
    private final String errorMessage;

    CreditorInstitutionErrorEnum(int status, String errorCode, String errorMessage) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static boolean isNotValidErrorCode(String errorCode) {
        return Arrays.stream(CreditorInstitutionErrorEnum.values())
                .noneMatch(elem -> elem.errorCode.equals(errorCode));
    }

    public static CreditorInstitutionErrorEnum getFromErrorCode(String errorCode) {
        return Arrays.stream(CreditorInstitutionErrorEnum.values())
                .filter(elem -> elem.errorCode.equals(errorCode))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
