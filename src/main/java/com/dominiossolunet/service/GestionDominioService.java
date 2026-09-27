package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GestionDominioService {

    private final DominioRepository dominioRepository;
    private final HistorialDominioRepository historialDominioRepository;
    private final TokenDominioRepository tokenDominioRepository;

    public GestionDominioService(
            DominioRepository dominioRepository,
            HistorialDominioRepository historialDominioRepository,
            TokenDominioRepository tokenDominioRepository) {

        this.dominioRepository = dominioRepository;
        this.historialDominioRepository = historialDominioRepository;
        this.tokenDominioRepository = tokenDominioRepository;
    }

    /**
     * Marca un dominio como renovado en el registrador.
     *
     * Para poder realizar la renovación:
     * - el dominio debe existir
     * - no debe estar ya renovado
     * - no debe estar rechazado
     * - el cliente debe haber confirmado la renovación
     *
     * La renovación se registra en estadoRenovacion y
     * se añade una entrada al historial.
     */
    @Transactional
    public void marcarComoRenovado(int idDominio) {

        Dominio dominio = dominioRepository.findById(idDominio)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Dominio no encontrado: " + idDominio
                        )
                );

        if (dominio.getEstadoRenovacion()
                == EstadoRenovacion.RENOVADO) {

            throw new IllegalStateException(
                    "El dominio ya está marcado como renovado: "
                            + dominio.getNombreDominio()
            );
        }

        if (dominio.getEstadoRenovacion()
                == EstadoRenovacion.RECHAZADO) {

            throw new IllegalStateException(
                    "El cliente ha rechazado la renovación del dominio: "
                            + dominio.getNombreDominio()
            );
        }

        TokenDominio ultimoTokenDominio = tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El dominio no tiene ninguna respuesta de renovación: "
                                        + dominio.getNombreDominio()
                        )
                );

        if (ultimoTokenDominio.getEstadoAvisoRenovacion()
                != EstadoAvisoRenovacion.CONFIRMADO) {

            throw new IllegalStateException(
                    "El cliente no ha confirmado la renovación del dominio: "
                            + dominio.getNombreDominio()
            );
        }

        /*
         * La renovación se ha realizado realmente en el registrador.
         */
        dominio.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );

        /*
         * Guardamos explícitamente el dominio actualizado.
         */
        dominioRepository.save(dominio);

        /*
         * Registramos la operación en el historial.
         */
        HistorialDominio historial = new HistorialDominio();

        historial.setDominio(dominio);
        historial.setTipoEvento(
                TipoEventoDominio.RENOVACION_REALIZADA
        );
        historial.setFecha(LocalDateTime.now());
        historial.setDetalle(
                "Renovación realizada en el registrador"
        );

        historialDominioRepository.save(historial);
    }
}
