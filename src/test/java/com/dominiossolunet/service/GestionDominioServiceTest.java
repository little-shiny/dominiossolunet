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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        dominio.setEstadoRenovacion(
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        tokenDominio = new TokenDominio();

        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.CONFIRMADO
        );
    }

    @Test
    void marcarComoRenovado_dominioConfirmado_loMarcaComoRenovado() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        gestionDominioService.marcarComoRenovado(1);

        assertThat(dominio.getEstadoRenovacion())
                .isEqualTo(EstadoRenovacion.RENOVADO);

        verify(dominioRepository)
                .save(dominio);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void marcarComoRenovado_guardaHistorialCorrectamente() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        gestionDominioService.marcarComoRenovado(1);

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository)
                .save(captor.capture());

        HistorialDominio historial =
                captor.getValue();

        assertThat(historial.getDominio())
                .isEqualTo(dominio);

        assertThat(historial.getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.RENOVACION_REALIZADA
                );

        assertThat(historial.getDetalle())
                .isEqualTo(
                        "Renovación realizada en el registrador"
                );

        assertThat(historial.getFecha())
                .isNotNull();
    }

    @Test
    void marcarComoRenovado_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "Dominio no encontrado: 1"
                );

        verify(dominioRepository)
                .findById(1);

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_dominioYaRenovado_lanzaExcepcion() {

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .contains(
                        "El dominio ya está marcado como renovado"
                );

        verify(dominioRepository, never())
                .save(any(Dominio.class));

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_dominioRechazado_lanzaExcepcion() {

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RECHAZADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .contains(
                        "El cliente ha rechazado la renovación"
                );

        verify(dominioRepository, never())
                .save(any(Dominio.class));

        verifyNoInteractions(
                tokenDominioRepository,
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_sinRespuestaDeRenovacion_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .contains(
                        "El dominio no tiene ninguna respuesta de renovación"
                );

        verify(dominioRepository, never())
                .save(any(Dominio.class));

        verifyNoInteractions(
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_clienteNoHaConfirmado_lanzaExcepcion() {

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .contains(
                        "El cliente no ha confirmado la renovación"
                );

        verify(dominioRepository, never())
                .save(any(Dominio.class));

        verifyNoInteractions(
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_clienteRechazo_lanzaExcepcion() {

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.RECHAZADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> gestionDominioService.marcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .contains(
                        "El cliente no ha confirmado la renovación"
                );

        verify(dominioRepository, never())
                .save(any(Dominio.class));

        verifyNoInteractions(
                historialDominioRepository
        );
    }

    @Test
    void marcarComoRenovado_renovacionCorrecta_soloModificaEstadoRenovacion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        gestionDominioService.marcarComoRenovado(1);

        assertThat(dominio.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.RENOVADO
                );

        verify(dominioRepository)
                .save(dominio);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void marcarComoRenovado_noCreaHistorialSiNoSePuedeRenovar() {

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RECHAZADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        assertThrows(
                IllegalStateException.class,
                () -> gestionDominioService.marcarComoRenovado(1)
        );

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));
    }
}