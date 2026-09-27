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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionDominioServiceTest {

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private HistorialDominioRepository historialDominioRepository;

    @Mock
    private TokenDominioRepository tokenDominioRepository;

    @InjectMocks
    private GestionDominioService gestionDominioService;

    private Dominio dominio;
    private TokenDominio tokenDominio;

    @BeforeEach
    void setUp() {
        dominio = new Dominio();
        dominio.setNombreDominio("ejemplo.com");
        dominio.setEstado(Estado.AVISO_ENVIADO);

        tokenDominio = new TokenDominio();
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.CONFIRMADO
        );
    }

    @Test
    void marcarComoRenovado_dominioExistente_cambiaEstadoYRegistraHistorial() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository.findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        gestionDominioService.marcarComoRenovado(1);

        assertEquals(Estado.ACTIVO, dominio.getEstado());

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository).save(captor.capture());

        HistorialDominio historial = captor.getValue();

        assertEquals(dominio, historial.getDominio());
        assertEquals(
                TipoEventoDominio.RENOVACION_REALIZADA,
                historial.getTipoEvento()
        );
        assertEquals(
                "Renovación realizada en el registrador",
                historial.getDetalle()
        );
        assertNotNull(historial.getFecha());
    }

    @Test
    void marcarComoRenovado_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        assertEquals(
                "Dominio no encontrado: 1",
                exception.getMessage()
        );

        verify(dominioRepository).findById(1);

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_dominioSinConfirmacion_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.RECHAZADO
        );

        when(tokenDominioRepository.findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        assertEquals(
                "El cliente no ha confirmado la renovación del dominio: ejemplo.com",
                exception.getMessage()
        );

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );

        verify(dominioRepository).findById(1);

        verify(tokenDominioRepository)
                .findFirstByDominioOrderByIdDesc(dominio);

        verifyNoInteractions(historialDominioRepository);
    }

    @Test
    void marcarComoRenovado_dominioYaActivo_lanzaExcepcion() {

        dominio.setEstado(Estado.ACTIVO);

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        assertEquals(
                "El dominio ya está activo: ejemplo.com",
                exception.getMessage()
        );

        verify(dominioRepository).findById(1);

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_dominioDeBaja_lanzaExcepcion() {

        dominio.setEstado(Estado.BAJA);

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        assertEquals(
                "No se puede renovar un dominio dado de baja: ejemplo.com",
                exception.getMessage()
        );

        verify(dominioRepository).findById(1);

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_sinTokenDeRenovacion_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository.findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        assertEquals(
                "El dominio no tiene ninguna respuesta de renovación: ejemplo.com",
                exception.getMessage()
        );

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );

        verify(dominioRepository).findById(1);

        verify(tokenDominioRepository)
                .findFirstByDominioOrderByIdDesc(dominio);

        verifyNoInteractions(historialDominioRepository);
    }
}