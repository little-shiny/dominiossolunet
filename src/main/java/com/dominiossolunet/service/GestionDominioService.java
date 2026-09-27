package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
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
            HistorialDominioRepository historialDominioRepository, TokenDominioRepository tokenDominioRepository) {

        this.dominioRepository = dominioRepository;
        this.historialDominioRepository = historialDominioRepository;
        this.tokenDominioRepository = tokenDominioRepository;
    }

    /**
     * Marca un dominio como renovado y registra el evento en el historial.
     */
    @Transactional
    public void marcarComoRenovado(int idDominio) {

        Dominio dominio = dominioRepository.findById(idDominio)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Dominio no encontrado: " + idDominio
                        )
                );

        TokenDominio tokenDominio =
                tokenDominioRepository
                        .findFirstByDominioAndEstadoAvisoRenovacionOrderByIdDesc(
                                dominio,
                                EstadoAvisoRenovacion.CONFIRMADO
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El cliente no ha aceptado renovar el dominio: "
                                                + dominio.getNombreDominio()
                                )
                        );

        dominio.setEstado(Estado.ACTIVO);

        HistorialDominio historial = new HistorialDominio();

        historial.setDominio(dominio);
        historial.setTipoEvento(TipoEventoDominio.RENOVACION_REALIZADA);
        historial.setFecha(LocalDateTime.now());
        historial.setDetalle("Renovación realizada en el registrador");

        historialDominioRepository.save(historial);
    }
}