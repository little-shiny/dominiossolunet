package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Mock
    private FacturacionRepository facturacionRepository;

    @InjectMocks
    private GestionDominioService gestionDominioService;

    private Dominio dominio;
    private TokenDominio tokenDominio;

    @BeforeEach
    void setUp() {

        dominio = new Dominio();
        dominio.setId(1);
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

    // =========================================================
    // marcarComoRenovado
    // =========================================================

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
                .isEqualTo("Dominio no encontrado: 1");

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

    // =========================================================
    // obtenerTodosLosDominios
    // =========================================================

    @Test
    void obtenerTodosLosDominios_devuelveTodosLosDominios() {

        Dominio dominio1 = new Dominio();
        dominio1.setId(1);
        dominio1.setNombreDominio("ejemplo.com");

        Dominio dominio2 = new Dominio();
        dominio2.setId(2);
        dominio2.setNombreDominio("ejemplo.es");

        List<Dominio> dominios =
                List.of(dominio1, dominio2);

        when(dominioRepository.findAll())
                .thenReturn(dominios);

        List<Dominio> resultado =
                gestionDominioService.obtenerTodosLosDominios();

        assertThat(resultado)
                .containsExactly(dominio1, dominio2);

        verify(dominioRepository)
                .findAll();
    }

    @Test
    void obtenerTodosLosDominios_sinDominios_devuelveListaVacia() {

        when(dominioRepository.findAll())
                .thenReturn(List.of());

        List<Dominio> resultado =
                gestionDominioService.obtenerTodosLosDominios();

        assertThat(resultado)
                .isEmpty();

        verify(dominioRepository)
                .findAll();
    }

    // =========================================================
    // obtenerDominio
    // =========================================================

    @Test
    void obtenerDominio_dominioExiste_devuelveDominio() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        Dominio resultado =
                gestionDominioService.obtenerDominio(1);

        assertThat(resultado)
                .isSameAs(dominio);

        verify(dominioRepository)
                .findById(1);
    }

    @Test
    void obtenerDominio_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestionDominioService.obtenerDominio(1)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Dominio no encontrado: 1");

        verify(dominioRepository)
                .findById(1);
    }

    // =========================================================
    // obtenerHistorial
    // =========================================================

    @Test
    void obtenerHistorial_dominioExiste_devuelveHistorial() {

        HistorialDominio historial1 =
                new HistorialDominio();

        historial1.setDominio(dominio);
        historial1.setTipoEvento(
                TipoEventoDominio.RENOVACION_REALIZADA
        );

        HistorialDominio historial2 =
                new HistorialDominio();

        historial2.setDominio(dominio);
        historial2.setTipoEvento(
                TipoEventoDominio.FACTURACION_REALIZADA
        );

        List<HistorialDominio> historial =
                List.of(historial1, historial2);

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(historialDominioRepository
                .findByDominioOrderByFechaDesc(dominio))
                .thenReturn(historial);

        List<HistorialDominio> resultado =
                gestionDominioService.obtenerHistorial(1);

        assertThat(resultado)
                .containsExactly(
                        historial1,
                        historial2
                );

        verify(dominioRepository)
                .findById(1);

        verify(historialDominioRepository)
                .findByDominioOrderByFechaDesc(dominio);
    }

    @Test
    void obtenerHistorial_dominioSinHistorial_devuelveListaVacia() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(historialDominioRepository
                .findByDominioOrderByFechaDesc(dominio))
                .thenReturn(List.of());

        List<HistorialDominio> resultado =
                gestionDominioService.obtenerHistorial(1);

        assertThat(resultado)
                .isEmpty();
    }

    @Test
    void obtenerHistorial_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestionDominioService.obtenerHistorial(1)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Dominio no encontrado: 1");

        verifyNoInteractions(
                historialDominioRepository
        );
    }

    // =========================================================
    // obtenerFacturacion
    // =========================================================

    @Test
    void obtenerFacturacion_dominioTieneFacturacion_devuelveFacturacion() {

        Facturacion facturacion =
                new Facturacion();

        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.of(facturacion));

        Optional<Facturacion> resultado =
                gestionDominioService.obtenerFacturacion(1);

        assertThat(resultado)
                .isPresent();

        assertThat(resultado.get())
                .isSameAs(facturacion);

        verify(dominioRepository)
                .findById(1);

        verify(facturacionRepository)
                .findByDominio(dominio);
    }

    @Test
    void obtenerFacturacion_dominioSinFacturacion_devuelveOptionalVacio() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(facturacionRepository.findByDominio(dominio))
                .thenReturn(Optional.empty());

        Optional<Facturacion> resultado =
                gestionDominioService.obtenerFacturacion(1);

        assertThat(resultado)
                .isEmpty();

        verify(facturacionRepository)
                .findByDominio(dominio);
    }

    @Test
    void obtenerFacturacion_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestionDominioService.obtenerFacturacion(1)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Dominio no encontrado: 1");

        verifyNoInteractions(
                facturacionRepository
        );
    }

    // =========================================================
    // puedeMarcarComoRenovado
    // =========================================================

    @Test
    void puedeMarcarComoRenovado_clienteConfirmo_devuelveTrue() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isTrue();
    }

    @Test
    void puedeMarcarComoRenovado_dominioYaRenovado_devuelveFalse() {

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isFalse();

        verify(tokenDominioRepository, never())
                .findFirstByDominioOrderByIdDesc(dominio);
    }

    @Test
    void puedeMarcarComoRenovado_dominioRechazado_devuelveFalse() {

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RECHAZADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isFalse();

        verify(tokenDominioRepository, never())
                .findFirstByDominioOrderByIdDesc(dominio);
    }

    @Test
    void puedeMarcarComoRenovado_sinToken_devuelveFalse() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.empty());

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isFalse();
    }

    @Test
    void puedeMarcarComoRenovado_tokenPendiente_devuelveFalse() {

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isFalse();
    }

    @Test
    void puedeMarcarComoRenovado_tokenRechazado_devuelveFalse() {

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.RECHAZADO
        );

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        when(tokenDominioRepository
                .findFirstByDominioOrderByIdDesc(dominio))
                .thenReturn(Optional.of(tokenDominio));

        boolean resultado =
                gestionDominioService
                        .puedeMarcarComoRenovado(1);

        assertThat(resultado)
                .isFalse();
    }

    @Test
    void puedeMarcarComoRenovado_dominioNoExiste_lanzaExcepcion() {

        when(dominioRepository.findById(1))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestionDominioService
                                .puedeMarcarComoRenovado(1)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Dominio no encontrado: 1");
    }
}
