package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturacionServiceTest {

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private FacturacionRepository facturacionRepository;

    @Mock
    private HistorialDominioRepository historialDominioRepository;

    @InjectMocks
    private FacturacionService facturacionService;

    private Dominio dominio;
    private Facturacion facturacion;

    @BeforeEach
    void setUp() {

        dominio = new Dominio();
        dominio.setNombreDominio("ejemplo.com");

        facturacion = new Facturacion();
        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );
    }

    @Test
    void marcarComoFacturado_dominioNoExiste_lanzaExcepcion() {

        // GIVEN
        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facturacionService.marcarComoFacturado(1)
        );

        // THEN
        assertEquals(
                "Dominio no encontrado 1",
                exception.getMessage()
        );

        verify(dominioRepository).findById(1);

        verifyNoInteractions(
                facturacionRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoFacturado_dominioSinFacturacion_lanzaExcepcion() {

        // GIVEN
        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.empty());

        // WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> facturacionService.marcarComoFacturado(1)
        );

        // THEN
        assertEquals(
                "El dominio no tiene un registro de facturación: ejemplo.com",
                exception.getMessage()
        );

        verify(dominioRepository).findById(1);

        verify(facturacionRepository)
                .findByDominio(dominio);

        verifyNoInteractions(
                historialDominioRepository
        );
    }

    @Test
    void marcarComoFacturado_dominioYaFacturado_lanzaExcepcion() {

        // GIVEN
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.FACTURADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.of(facturacion));

        // WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> facturacionService.marcarComoFacturado(1)
        );

        // THEN
        assertEquals(
                "El dominio ya está facturado: ejemplo.com",
                exception.getMessage()
        );

        verify(dominioRepository)
                .findById(1);

        verify(facturacionRepository)
                .findByDominio(dominio);

        verifyNoInteractions(
                historialDominioRepository
        );

        assertEquals(
                EstadoFacturacion.FACTURADO,
                facturacion.getEstadoFacturacion()
        );
    }

    @Test
    void marcarComoFacturado_pendiente_cambiaEstadoYFecha() {

        // GIVEN
        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.of(facturacion));

        // WHEN
        facturacionService.marcarComoFacturado(1);

        // THEN
        assertEquals(
                EstadoFacturacion.FACTURADO,
                facturacion.getEstadoFacturacion()
        );

        assertEquals(
                LocalDate.now(),
                facturacion.getFechaUltimaFactura()
        );

        verify(dominioRepository)
                .findById(1);

        verify(facturacionRepository)
                .findByDominio(dominio);
    }

    @Test
    void marcarComoFacturado_pendiente_guardaHistorial() {

        // GIVEN
        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.of(facturacion));

        // WHEN
        facturacionService.marcarComoFacturado(1);

        // THEN
        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository)
                .save(captor.capture());

        HistorialDominio historial = captor.getValue();

        assertEquals(
                dominio,
                historial.getDominio()
        );

        assertEquals(
                TipoEventoDominio.FACTURACION_REALIZADA,
                historial.getTipoEvento()
        );

        assertEquals(
                "Facturación realizada",
                historial.getDetalle()
        );

        assertNotNull(
                historial.getFecha()
        );
    }

    @Test
    void marcarComoFacturado_pendiente_realizaTodasLasOperaciones() {

        // GIVEN
        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.of(facturacion));

        // WHEN
        facturacionService.marcarComoFacturado(1);

        // THEN
        assertEquals(
                EstadoFacturacion.FACTURADO,
                facturacion.getEstadoFacturacion()
        );

        assertNotNull(
                facturacion.getFechaUltimaFactura()
        );

        verify(dominioRepository)
                .findById(1);

        verify(facturacionRepository)
                .findByDominio(dominio);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));

        verifyNoMoreInteractions(
                dominioRepository,
                facturacionRepository,
                historialDominioRepository
        );
    }
}