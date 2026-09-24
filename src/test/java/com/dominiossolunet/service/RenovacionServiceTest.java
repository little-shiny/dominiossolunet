package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.utils.RenovacionUrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenovacionServiceTest {

    private final List<Integer> UMBRALES = List.of(30, 15, 5, 1);

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private RenovacionUrlBuilder renovacionUrlBuilder;

    @Mock
    private EmailService emailService;

    private RenovacionService renovacionService;

    @BeforeEach
    void setUp() {

        renovacionService = new RenovacionService(dominioRepository, tokenService, emailService, renovacionUrlBuilder);

        ReflectionTestUtils.setField(renovacionService, "umbrales", UMBRALES);

        ReflectionTestUtils.invokeMethod(renovacionService, "calcularUmbralMaximo");
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void procesarAvisos_noHayCandidatos_noHaceNada() {

        // Arrange
        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of());

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verify(dominioRepository).findByEstadoInAndFechaExpiracionLessThanEqual(eq(List.of(Estado.ACTIVO, Estado.AVISO_ENVIADO)), eq(LocalDate.now().plusDays(30)));

        verifyNoInteractions(tokenService);

        verify(emailService).enviarInformeRenovacion(eq(List.of()));
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void procesarAvisos_dominioDentroDe30Dias_generaAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(30));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(Estado.AVISO_ENVIADO, dominio.getEstado());

        assertEquals(30, dominio.getUltimoUmbralAvisado());

        assertEquals(LocalDate.now(), dominio.getUltimoAviso());

        verify(tokenService).generarToken(eq(cliente), eq(List.of(dominio)));

        verify(emailService).enviarAvisoRenovacion(eq(cliente), eq(List.of(dominio)), eq("http://localhost/renovacion/test"));

        verify(emailService).enviarInformeRenovacion(eq(List.of()));
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void procesarAvisos_dominioA27Dias_utilizaUmbral30() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(27));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(30, dominio.getUltimoUmbralAvisado());

        assertEquals(Estado.AVISO_ENVIADO, dominio.getEstado());
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void procesarAvisos_dominioA15Dias_utilizaUmbral15() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(15));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(15, dominio.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void procesarAvisos_dominioA5Dias_utilizaUmbral5() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(5));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(5, dominio.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void procesarAvisos_dominioA1Dia_utilizaUmbral1() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(1));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(1, dominio.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void procesarAvisos_dominioYaExpirado_noGeneraAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().minusDays(1));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertNull(dominio.getUltimoUmbralAvisado());

        verify(emailService).enviarInformeRenovacion(eq(List.of()));
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void procesarAvisos_mismoUmbralYaAvisado_noGeneraNuevoAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(27));

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertEquals(30, dominio.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void procesarAvisos_cambiaDeUmbral_generaNuevoAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(14));

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(15, dominio.getUltimoUmbralAvisado());

        assertEquals(Estado.AVISO_ENVIADO, dominio.getEstado());

        assertEquals(LocalDate.now(), dominio.getUltimoAviso());

        verify(tokenService).generarToken(eq(cliente), eq(List.of(dominio)));
    }

    // =========================================================
    // TEST 10
    // =========================================================

    @Test
    void procesarAvisos_dosDominiosMismoCliente_generaUnSoloToken() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio1 = crearDominio(cliente, LocalDate.now().plusDays(27));

        Dominio dominio2 = crearDominio(cliente, LocalDate.now().plusDays(10));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio1, dominio2));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

        @SuppressWarnings("unchecked") ArgumentCaptor<List<Dominio>> dominiosCaptor = ArgumentCaptor.forClass(List.class);

        verify(tokenService, times(1)).generarToken(clienteCaptor.capture(), dominiosCaptor.capture());

        assertSame(cliente, clienteCaptor.getValue());

        assertEquals(2, dominiosCaptor.getValue().size());

        assertTrue(dominiosCaptor.getValue().contains(dominio1));

        assertTrue(dominiosCaptor.getValue().contains(dominio2));
    }

    // =========================================================
    // TEST 11
    // =========================================================

    @Test
    void procesarAvisos_dosClientes_generaUnTokenPorCliente() {

        // Arrange
        Cliente cliente1 = crearCliente("Ana", "ana@test.com");

        Cliente cliente2 = crearCliente("Laura", "laura@test.com");

        Dominio dominio1 = crearDominio(cliente1, LocalDate.now().plusDays(20));

        Dominio dominio2 = crearDominio(cliente2, LocalDate.now().plusDays(10));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio1, dominio2));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

        @SuppressWarnings("unchecked") ArgumentCaptor<List<Dominio>> dominiosCaptor = ArgumentCaptor.forClass(List.class);

        verify(tokenService, times(2)).generarToken(clienteCaptor.capture(), dominiosCaptor.capture());

        assertEquals(2, clienteCaptor.getAllValues().size());

        assertTrue(clienteCaptor.getAllValues().contains(cliente1));

        assertTrue(clienteCaptor.getAllValues().contains(cliente2));
    }

    // =========================================================
    // TEST 12
    // =========================================================

    @Test
    void procesarAvisos_dominioFueraDeUmbrales_noGeneraAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(40));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertNull(dominio.getUltimoUmbralAvisado());

        assertNull(dominio.getUltimoAviso());
    }

    // =========================================================
    // TEST 13
    // =========================================================

    @Test
    void procesarAvisos_actualizaEstadoDelDominio() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(20));

        dominio.setEstado(Estado.ACTIVO);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(Estado.AVISO_ENVIADO, dominio.getEstado());

        assertEquals(30, dominio.getUltimoUmbralAvisado());

        assertEquals(LocalDate.now(), dominio.getUltimoAviso());
    }

    // =========================================================
    // TEST 14
    // =========================================================

    @Test
    void procesarAvisos_dominioConUltimoAvisoAntiguoMismoUmbral_noRepiteAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(25));

        dominio.setUltimoUmbralAvisado(30);
        dominio.setUltimoAviso(LocalDate.now().minusDays(5));
        dominio.setEstado(Estado.AVISO_ENVIADO);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertEquals(LocalDate.now().minusDays(5), dominio.getUltimoAviso());

        assertEquals(30, dominio.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 15
    // =========================================================

    @Test
    void calcularUmbralMaximo_sinUmbrales_lanzaExcepcion() {

        // Arrange
        RenovacionService service = new RenovacionService(dominioRepository, tokenService, emailService, renovacionUrlBuilder);

        ReflectionTestUtils.setField(service, "umbrales", List.of());

        // Act + Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(service, "calcularUmbralMaximo"));

        assertEquals("Debe existir al menos un umbral de renovación configurado", exception.getMessage());
    }

    // =========================================================
    // TEST 16
    // =========================================================

    @Test
    void calcularUmbralMaximo_conUmbralesConfigurados_calculaMayorUmbral() {

        // Arrange
        RenovacionService service = new RenovacionService(dominioRepository, tokenService, emailService, renovacionUrlBuilder);

        ReflectionTestUtils.setField(service, "umbrales", List.of(1, 5, 15, 30));

        // Act
        ReflectionTestUtils.invokeMethod(service, "calcularUmbralMaximo");

        // Assert
        int umbralMaximo = (int) ReflectionTestUtils.getField(service, "umbralMaximo");

        assertEquals(30, umbralMaximo);
    }

    // =========================================================
    // TEST 17
    // =========================================================

    @Test
    void procesarAvisos_emailFalla_noMarcaDominioYRegistraError() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(20));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        TokenCliente token = crearToken("token-test");

        when(tokenService.generarToken(any(Cliente.class), anyList())).thenReturn(token);

        when(renovacionUrlBuilder.construirUrlConfirmacion(anyString())).thenReturn("http://localhost/renovacion/test");

        when(emailService.enviarAvisoRenovacion(any(), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(false, "Error de prueba"));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(Estado.ACTIVO, dominio.getEstado());

        assertNull(dominio.getUltimoUmbralAvisado());

        assertNull(dominio.getUltimoAviso());

        verify(tokenService).generarToken(eq(cliente), eq(List.of(dominio)));

        verify(emailService).enviarAvisoRenovacion(eq(cliente), eq(List.of(dominio)), eq("http://localhost/renovacion/test"));

        ArgumentCaptor<List<ErrorEnvioEmail>> erroresCaptor = ArgumentCaptor.forClass(List.class);

        verify(emailService).enviarInformeRenovacion(erroresCaptor.capture());

        assertEquals(1, erroresCaptor.getValue().size());

        ErrorEnvioEmail error = erroresCaptor.getValue().get(0);

        assertSame(cliente, error.getCliente());

        assertEquals(List.of(dominio), error.getDominios());

        assertEquals("Error de prueba", error.getMensajeError());
    }

    // =========================================================
    // TEST 18
    // =========================================================

    @Test
    void procesarAvisos_unClienteFalla_otroClienteContinua() {

        // Arrange
        Cliente cliente1 = crearCliente("Ana", "ana@test.com");

        Cliente cliente2 = crearCliente("Laura", "laura@test.com");

        Dominio dominio1 = crearDominio(cliente1, LocalDate.now().plusDays(20));

        Dominio dominio2 = crearDominio(cliente2, LocalDate.now().plusDays(10));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio1, dominio2));

        TokenCliente token = crearToken("token-test");

        when(tokenService.generarToken(any(Cliente.class), anyList())).thenReturn(token);

        when(renovacionUrlBuilder.construirUrlConfirmacion(anyString())).thenReturn("http://localhost/renovacion/test");

        when(emailService.enviarAvisoRenovacion(eq(cliente1), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(false, "Error cliente 1"));

        when(emailService.enviarAvisoRenovacion(eq(cliente2), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(true, null));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(Estado.ACTIVO, dominio1.getEstado());

        assertEquals(Estado.AVISO_ENVIADO, dominio2.getEstado());

        verify(emailService, times(2)).enviarAvisoRenovacion(any(), anyList(), anyString());

        ArgumentCaptor<List<ErrorEnvioEmail>> erroresCaptor = ArgumentCaptor.forClass(List.class);

        verify(emailService).enviarInformeRenovacion(erroresCaptor.capture());

        assertEquals(1, erroresCaptor.getValue().size());

        assertSame(cliente1, erroresCaptor.getValue().get(0).getCliente());
    }

    // =========================================================
    // TEST 19
    // =========================================================

    @Test
    void procesarAvisos_emailCorrecto_marcaTodosLosDominiosDelCliente() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio1 = crearDominio(cliente, LocalDate.now().plusDays(25));

        Dominio dominio2 = crearDominio(cliente, LocalDate.now().plusDays(10));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio1, dominio2));

        prepararEnvioEmailCorrecto();

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(Estado.AVISO_ENVIADO, dominio1.getEstado());

        assertEquals(Estado.AVISO_ENVIADO, dominio2.getEstado());

        assertEquals(30, dominio1.getUltimoUmbralAvisado());

        assertEquals(15, dominio2.getUltimoUmbralAvisado());
    }

    // =========================================================
    // TEST 20
    // =========================================================

    @Test
    void procesarAvisos_emailFalla_seGeneraInformeConError() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(20));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        TokenCliente token = crearToken("token-test");

        when(tokenService.generarToken(any(Cliente.class), anyList())).thenReturn(token);

        when(renovacionUrlBuilder.construirUrlConfirmacion(anyString())).thenReturn("http://localhost/renovacion/test");

        when(emailService.enviarAvisoRenovacion(any(), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(false, "No se pudo conectar con el servidor SMTP"));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        ArgumentCaptor<List<ErrorEnvioEmail>> captor = ArgumentCaptor.forClass(List.class);

        verify(emailService).enviarInformeRenovacion(captor.capture());

        List<ErrorEnvioEmail> errores = captor.getValue();

        assertEquals(1, errores.size());

        assertEquals("No se pudo conectar con el servidor SMTP", errores.get(0).getMensajeError());
    }

    // =========================================================
    // TEST 21
    // =========================================================

    @Test
    void procesarAvisos_emailFalla_tokenSeGeneraIgualmente() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(cliente, LocalDate.now().plusDays(20));

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(anyList(), any(LocalDate.class))).thenReturn(List.of(dominio));

        TokenCliente token = crearToken("token-test");

        when(tokenService.generarToken(any(Cliente.class), anyList())).thenReturn(token);

        when(renovacionUrlBuilder.construirUrlConfirmacion(anyString())).thenReturn("http://localhost/renovacion/test");

        when(emailService.enviarAvisoRenovacion(any(), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(false, "Error de prueba"));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verify(tokenService).generarToken(eq(cliente), eq(List.of(dominio)));

        assertEquals(Estado.ACTIVO, dominio.getEstado());
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private void prepararEnvioEmailCorrecto() {

        TokenCliente token = crearToken("token-test");

        when(tokenService.generarToken(any(Cliente.class), anyList())).thenReturn(token);

        when(renovacionUrlBuilder.construirUrlConfirmacion(anyString())).thenReturn("http://localhost/renovacion/test");

        when(emailService.enviarAvisoRenovacion(any(), anyList(), anyString())).thenReturn(new ResultadoEnvioEmail(true, null));

        when(emailService.enviarInformeRenovacion(anyList())).thenReturn(new ResultadoEnvioEmail(true, null));
    }

    private TokenCliente crearToken(String valor) {

        TokenCliente token = new TokenCliente();
        token.setToken(valor);

        return token;
    }

    private Cliente crearCliente(String nombre, String email) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return cliente;
    }

    private Dominio crearDominio(Cliente cliente, LocalDate fechaExpiracion) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio("dominio-test.com");
        dominio.setFechaExpiracion(fechaExpiracion);
        dominio.setEstado(Estado.ACTIVO);

        return dominio;
    }
}
