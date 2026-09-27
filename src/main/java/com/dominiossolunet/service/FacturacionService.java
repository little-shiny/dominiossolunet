package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@Service
public class FacturacionService {

    private final DominioRepository dominioRepository;
    private final FacturacionRepository facturacionRepository;
    private final HistorialDominioRepository historialDominioRepository;

    private static final Logger logger = LoggerFactory.getLogger(FacturacionService.class);
    public FacturacionService(DominioRepository dominioRepository, FacturacionRepository facturacionRepository,
                              HistorialDominioRepository historialDominioRepository){
        this.dominioRepository = dominioRepository;
        this.facturacionRepository = facturacionRepository;
        this.historialDominioRepository = historialDominioRepository;
    }

    /**
     * Marca la facturacion de un dominio como realizada y registra el evento en el historial.
     */
    @Transactional
    public void marcarComoFacturado(int idDominio){

        Dominio dominio = dominioRepository.findById(idDominio).orElseThrow(()-> new IllegalArgumentException(
                "Dominio no encontrado " + idDominio));
        logger.warn("Dominio no encontrado, ID : {}", idDominio);

        Facturacion facturacion =
                facturacionRepository.findByDominio(dominio).orElseThrow(()-> new IllegalStateException("El dominio " +
                        "no tiene un registro de facturación: " + dominio.getNombreDominio()));
        logger.warn("El dominio no tiene registro de facturacion: {}", dominio.getNombreDominio());

        if(facturacion.getEstadoFacturacion() == EstadoFacturacion.FACTURADO){
            logger.info("el dominio {} ya está facturado", dominio.getNombreDominio());
            throw new IllegalStateException("El dominio ya está facturado: " + dominio.getNombreDominio());

        }

        facturacion.setEstadoFacturacion(EstadoFacturacion.FACTURADO);

        facturacion.setFechaUltimaFactura(LocalDate.now());

        HistorialDominio historial = new HistorialDominio();
        historial.setDominio(dominio);
        historial.setTipoEvento(TipoEventoDominio.FACTURACION_REALIZADA);
        historial.setFecha(java.time.LocalDateTime.now());
        historial.setDetalle("Facturación realizada");

        historialDominioRepository.save(historial);
        logger.info("Historial de facturación creado");
    }
}
