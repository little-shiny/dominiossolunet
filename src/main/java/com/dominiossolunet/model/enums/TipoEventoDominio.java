package com.dominiossolunet.model.enums;

/**
 * Eventos de NEGOCIO NO ESTADOS, solo usados en el bloque de renovacion/facturación
 */

public enum TipoEventoDominio {

    AVISO_RENOVACION_ENVIADO,

    CLIENTE_ACEPTA_RENOVACION,

    CLIENTE_RECHAZA_RENOVACION,

    RENOVACION_REALIZADA,

    FACTURACION_REALIZADA
}