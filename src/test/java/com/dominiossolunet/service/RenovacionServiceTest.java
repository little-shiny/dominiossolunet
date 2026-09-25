package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.utils.RenovacionUrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenovacionServiceTest {

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private RenovacionUrlBuilder renovacionUrlBuilder;

    @Mock
    private HistorialDominioRepository historialDominioRepository;

    @InjectMocks
    private RenovacionService renovacionService;

    private Cliente cliente;

    private Dominio dominio;

    private TokenCliente token;

    @BeforeEach
    void setUp() {

        cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setCliente(cliente);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );

        token = new TokenCliente();
        token.setToken("token123");
        token.setCliente(cliente);

        /*
         * El servicio obtiene los umbrales desde
         * renovacion.umbrales.
         *
         * En los tests los inyectamos manualmente.
         */
        ReflectionTestUtils.setField(
                renovacionService,
                "umbrales",
                List.of(30, 15, 5, 1)
        );

        ReflectionTestUtils.setField(
                renovacionService,
                "umbralMaximo",
                30
        );
    }

    // ============================================================
    // TESTS PROCESAR AVISOS
    // ============================================================

    @Test
    void procesarAvisos_sinDominiosNoHaceNada() {

        // Arrange

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of());

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(
                                true,
                                null
                        )
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, never())
                .generarToken(any(), anyList());

        verify(emailService, never())
                .enviarAvisoRenovacion(
                        any(),
                        anyList(),
                        anyString()
                );

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioDentroDe30Dias_generaAviso() {

        // Arrange

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("http://localhost/renovar/token123");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("http://localhost/renovar/token123")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService)
                .generarToken(
                        eq(cliente),
                        anyList()
                );

        verify(emailService)
                .enviarAvisoRenovacion(
                        eq(cliente),
                        anyList(),
                        eq("http://localhost/renovar/token123")
                );

        assertThat(dominio.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(30);

        assertThat(dominio.getUltimoAviso())
                .isEqualTo(LocalDate.now());

        /*
         * Nuevo comportamiento:
         * cada dominio avisado correctamente genera
         * un evento de historial.
         */
        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository)
                .save(captor.capture());

        HistorialDominio historial =
                captor.getValue();

        assertThat(historial.getDominio())
                .isSameAs(dominio);

        assertThat(historial.getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.AVISO_RENOVACION_ENVIADO
                );

        assertThat(historial.getFecha())
                .isNotNull();
    }

    @Test
    void procesarAvisos_dominioA27Dias_utilizaUmbral30() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(27)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(30);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioA15Dias_utilizaUmbral15() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(15)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(15);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioA5Dias_utilizaUmbral5() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(5)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(5);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioA1Dia_utilizaUmbral1() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(1)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(1);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioExpirado_noGeneraAviso() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().minusDays(1)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, never())
                .generarToken(any(), anyList());

        verify(emailService, never())
                .enviarAvisoRenovacion(
                        any(),
                        anyList(),
                        anyString()
                );

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_mismoUmbralYaAvisado_noGeneraNuevoAviso() {

        // Arrange

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, never())
                .generarToken(any(), anyList());

        verify(emailService, never())
                .enviarAvisoRenovacion(
                        any(),
                        anyList(),
                        anyString()
                );

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_cambiaUmbral_generaNuevoAviso() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(15)
        );

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(15);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    // ============================================================
    // TESTS AGRUPACIÓN POR CLIENTE
    // ============================================================

    @Test
    void procesarAvisos_dosDominiosMismoCliente_generaUnSoloToken() {

        // Arrange

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("ana.es");
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setCliente(cliente);
        dominio2.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio, dominio2));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, times(1))
                .generarToken(
                        eq(cliente),
                        anyList()
                );

        verify(emailService, times(1))
                .enviarAvisoRenovacion(
                        eq(cliente),
                        anyList(),
                        eq("url")
                );

        /*
         * Aunque el email sea único, se registra
         * un historial independiente para cada dominio.
         */
        verify(historialDominioRepository, times(2))
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dosClientes_generaUnTokenPorCliente() {

        // Arrange

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Luis");
        cliente2.setEmail("luis@luis.com");

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("luis.es");
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setCliente(cliente2);
        dominio2.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );

        TokenCliente token2 = new TokenCliente();
        token2.setToken("token456");
        token2.setCliente(cliente2);

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio, dominio2));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(tokenService.generarToken(
                eq(cliente2),
                anyList()
        )).thenReturn(token2);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion(anyString()))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                any(),
                anyList(),
                anyString()
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, times(2))
                .generarToken(
                        any(Cliente.class),
                        anyList()
                );

        verify(emailService, times(2))
                .enviarAvisoRenovacion(
                        any(Cliente.class),
                        anyList(),
                        anyString()
                );

        /*
         * Un historial por cada dominio.
         */
        verify(historialDominioRepository, times(2))
                .save(any(HistorialDominio.class));
    }

    @Test
    void procesarAvisos_dominioFueraDeUmbral_noGeneraAviso() {

        // Arrange

        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(31)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of());

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(tokenService, never())
                .generarToken(any(), anyList());

        verify(emailService, never())
                .enviarAvisoRenovacion(
                        any(),
                        anyList(),
                        anyString()
                );

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));
    }

    // ============================================================
    // TESTS HISTORIAL DE RENOVACIÓN
    // ============================================================

    @Test
    void procesarAvisos_emailEnviado_guardaHistorialPorCadaDominio() {

        // Arrange

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("ana.es");
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setCliente(cliente);
        dominio2.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio, dominio2));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository, times(2))
                .save(captor.capture());

        List<HistorialDominio> historiales =
                captor.getAllValues();

        assertThat(historiales)
                .hasSize(2);

        assertThat(historiales)
                .allMatch(historial ->
                        historial.getTipoEvento()
                                == TipoEventoDominio.AVISO_RENOVACION_ENVIADO
                );

        assertThat(historiales)
                .extracting(HistorialDominio::getDominio)
                .containsExactlyInAnyOrder(
                        dominio,
                        dominio2
                );

        assertThat(historiales)
                .allMatch(historial ->
                        historial.getFecha() != null
                );
    }

    @Test
    void procesarAvisos_emailNoEnviado_noGuardaHistorial() {

        // Arrange

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(
                        false,
                        "Error enviando correo"
                )
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));

        /*
         * Si el email falla, el dominio no se marca
         * como avisado.
         */
        assertThat(dominio.getEstado())
                .isNotEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominio.getUltimoUmbralAvisado())
                .isNull();

        assertThat(dominio.getUltimoAviso())
                .isNull();
    }

    @Test
    void procesarAvisos_emailEnviado_marcaDominioComoAvisado() {

        // Arrange

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(true, null)
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominio.getUltimoAviso())
                .isEqualTo(LocalDate.now());

        assertThat(dominio.getUltimoUmbralAvisado())
                .isEqualTo(30);

        verify(historialDominioRepository)
                .save(any(HistorialDominio.class));
    }

    // ============================================================
    // TESTS ERROR ENVÍO EMAIL
    // ============================================================

    @Test
    void procesarAvisos_errorEnEnvio_guardaErrorYNoMarcaDominio() {

        // Arrange

        when(dominioRepository
                .findByEstadoInAndFechaExpiracionLessThanEqual(
                        anyList(),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        when(renovacionUrlBuilder
                .construirUrlConfirmacion("token123"))
                .thenReturn("url");

        when(emailService.enviarAvisoRenovacion(
                eq(cliente),
                anyList(),
                eq("url")
        )).thenReturn(
                new ResultadoEnvioEmail(
                        false,
                        "Error enviando correo"
                )
        );

        when(emailService.enviarInformeRenovacion(anyList()))
                .thenReturn(
                        new ResultadoEnvioEmail(true, null)
                );

        // Act

        renovacionService.procesarAvisos();

        // Assert

        assertThat(dominio.getEstado())
                .isNotEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominio.getUltimoUmbralAvisado())
                .isNull();

        assertThat(dominio.getUltimoAviso())
                .isNull();

        verify(historialDominioRepository, never())
                .save(any(HistorialDominio.class));

        /*
         * El informe debe recibir el error del envío.
         */
        verify(emailService)
                .enviarInformeRenovacion(
                        anyList()
                );
    }
}
