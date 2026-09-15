package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.repository.DominioRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de RenovacionService.
 * <p>
 * Se comprueba:
 * <p>
 * - Búsqueda de dominios candidatos.
 * - Ignorar dominios expirados.
 * - Determinación de umbrales.
 * - Evitar avisos duplicados.
 * - Actualización del dominio.
 * - Agrupación por cliente.
 * - Generación de un único token por cliente.
 * - Envío de todos los dominios correspondientes al cliente
 * al TokenService.
 */
@ExtendWith(MockitoExtension.class)
class RenovacionServiceTest {

    private final List<Integer> UMBRALES = List.of(30, 15, 5, 1);
    @Mock
    private DominioRepository dominioRepository;
    @Mock
    private TokenService tokenService;
    @InjectMocks
    private RenovacionService renovacionService;

    @BeforeEach
    void setUp() {

        /*
         * @Value no se ejecuta en un test unitario con Mockito,
         * por lo que inyectamos manualmente los umbrales.
         */
        ReflectionTestUtils.setField(
                renovacionService,
                "umbrales",
                UMBRALES
        );

        /*
         * Simulamos el comportamiento de @PostConstruct.
         */
        ReflectionTestUtils.invokeMethod(
                renovacionService,
                "calcularUmbralMaximo"
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void procesarAvisos_noHayCandidatos_noHaceNada() {

        // Arrange
        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of());

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verify(dominioRepository).findByEstadoInAndFechaExpiracionLessThanEqual(
                eq(List.of(
                        Estado.ACTIVO,
                        Estado.AVISO_ENVIADO
                )),
                eq(LocalDate.now().plusDays(30))
        );

        verifyNoInteractions(tokenService);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void procesarAvisos_dominioDentroDe30Dias_generaAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(30)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        TokenCliente token = new TokenCliente();

        when(tokenService.generarToken(
                eq(cliente),
                anyList()
        )).thenReturn(token);

        // Act
        renovacionService.procesarAvisos();

        // Assert

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );

        assertEquals(
                30,
                dominio.getUltimoUmbralAvisado()
        );

        assertEquals(
                LocalDate.now(),
                dominio.getUltimoAviso()
        );

        verify(tokenService).generarToken(
                eq(cliente),
                eq(List.of(dominio))
        );
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void procesarAvisos_dominioA27Dias_utilizaUmbral30() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(27)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(
                30,
                dominio.getUltimoUmbralAvisado()
        );

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void procesarAvisos_dominioA15Dias_utilizaUmbral15() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(15)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(
                15,
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void procesarAvisos_dominioA5Dias_utilizaUmbral5() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(5)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(
                5,
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void procesarAvisos_dominioA1Dia_utilizaUmbral1() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(1)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert
        assertEquals(
                1,
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void procesarAvisos_dominioYaExpirado_noGeneraAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().minusDays(1)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertNull(
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void procesarAvisos_mismoUmbralYaAvisado_noGeneraNuevoAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(27)
        );

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        // Act
        renovacionService.procesarAvisos();

        // Assert
        verifyNoInteractions(tokenService);

        assertEquals(
                30,
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void procesarAvisos_cambiaDeUmbral_generaNuevoAviso() {

        // Arrange
        Cliente cliente = crearCliente("Ana", "ana@test.com");

        /*
         * El dominio está a 14 días.
         *
         * Su último aviso fue a los 30 días.
         *
         * Ahora corresponde el umbral de 15.
         */
        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(14)
        );

        dominio.setUltimoUmbralAvisado(30);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert

        assertEquals(
                15,
                dominio.getUltimoUmbralAvisado()
        );

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );

        assertEquals(
                LocalDate.now(),
                dominio.getUltimoAviso()
        );

        verify(tokenService).generarToken(
                eq(cliente),
                eq(List.of(dominio))
        );
    }

    // =========================================================
    // TEST 10
    // =========================================================

    @Test
    void procesarAvisos_dosDominiosMismoCliente_generaUnSoloToken() {

        // Arrange

        Cliente cliente = crearCliente(
                "Ana",
                "ana@test.com"
        );

        Dominio dominio1 = crearDominio(
                cliente,
                LocalDate.now().plusDays(27)
        );

        Dominio dominio2 = crearDominio(
                cliente,
                LocalDate.now().plusDays(10)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(
                dominio1,
                dominio2
        ));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert

        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Dominio>> dominiosCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(tokenService, times(1)).generarToken(
                clienteCaptor.capture(),
                dominiosCaptor.capture()
        );

        assertSame(
                cliente,
                clienteCaptor.getValue()
        );

        assertEquals(
                2,
                dominiosCaptor.getValue().size()
        );

        assertTrue(
                dominiosCaptor.getValue().contains(dominio1)
        );

        assertTrue(
                dominiosCaptor.getValue().contains(dominio2)
        );
    }

    // =========================================================
    // TEST 11
    // =========================================================

    @Test
    void procesarAvisos_dosClientes_generaUnTokenPorCliente() {

        // Arrange

        Cliente cliente1 = crearCliente(
                "Ana",
                "ana@test.com"
        );

        Cliente cliente2 = crearCliente(
                "Laura",
                "laura@test.com"
        );

        Dominio dominio1 = crearDominio(
                cliente1,
                LocalDate.now().plusDays(20)
        );

        Dominio dominio2 = crearDominio(
                cliente2,
                LocalDate.now().plusDays(10)
        );

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(
                dominio1,
                dominio2
        ));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert

        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Dominio>> dominiosCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(tokenService, times(2)).generarToken(
                clienteCaptor.capture(),
                dominiosCaptor.capture()
        );

        assertEquals(
                2,
                clienteCaptor.getAllValues().size()
        );

        assertTrue(
                clienteCaptor.getAllValues().contains(cliente1)
        );

        assertTrue(
                clienteCaptor.getAllValues().contains(cliente2)
        );
    }

    // =========================================================
    // TEST 12
    // =========================================================

    @Test
    void procesarAvisos_dominioFueraDeUmbrales_noGeneraAviso() {

        // Arrange

        Cliente cliente = crearCliente(
                "Ana",
                "ana@test.com"
        );

        /*
         * El dominio está a 40 días.
         *
         * El mayor umbral configurado es 30.
         */
        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(40)
        );

        /*
         * Lo devolvemos manualmente desde el repository para
         * comprobar que el servicio también protege este caso.
         */
        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        // Act
        renovacionService.procesarAvisos();

        // Assert

        verifyNoInteractions(tokenService);

        assertNull(
                dominio.getUltimoUmbralAvisado()
        );

        assertNull(
                dominio.getUltimoAviso()
        );
    }

    // =========================================================
    // TEST 13
    // =========================================================

    @Test
    void procesarAvisos_actualizaEstadoDelDominio() {

        // Arrange

        Cliente cliente = crearCliente(
                "Ana",
                "ana@test.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(20)
        );

        dominio.setEstado(Estado.ACTIVO);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        when(tokenService.generarToken(
                any(Cliente.class),
                anyList()
        )).thenReturn(new TokenCliente());

        // Act
        renovacionService.procesarAvisos();

        // Assert

        assertEquals(
                Estado.AVISO_ENVIADO,
                dominio.getEstado()
        );

        assertEquals(
                30,
                dominio.getUltimoUmbralAvisado()
        );

        assertEquals(
                LocalDate.now(),
                dominio.getUltimoAviso()
        );
    }

    // =========================================================
    // TEST 14
    // =========================================================

    @Test
    void procesarAvisos_dominioConUltimoAvisoAntiguoMismoUmbral_noRepiteAviso() {

        // Arrange

        Cliente cliente = crearCliente(
                "Ana",
                "ana@test.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                LocalDate.now().plusDays(25)
        );

        dominio.setUltimoUmbralAvisado(30);
        dominio.setUltimoAviso(LocalDate.now().minusDays(5));
        dominio.setEstado(Estado.AVISO_ENVIADO);

        when(dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                anyList(),
                any(LocalDate.class)
        )).thenReturn(List.of(dominio));

        // Act
        renovacionService.procesarAvisos();

        // Assert

        verifyNoInteractions(tokenService);

        /*
         * La fecha del último aviso tampoco debe modificarse.
         */
        assertEquals(
                LocalDate.now().minusDays(5),
                dominio.getUltimoAviso()
        );

        assertEquals(
                30,
                dominio.getUltimoUmbralAvisado()
        );
    }

    // =========================================================
    // TEST 15
    // =========================================================

    @Test
    void calcularUmbralMaximo_sinUmbrales_lanzaExcepcion() {

        // Arrange

        RenovacionService service =
                new RenovacionService(
                        dominioRepository,
                        tokenService
                );

        ReflectionTestUtils.setField(
                service,
                "umbrales",
                List.of()
        );

        // Act + Assert

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                service,
                                "calcularUmbralMaximo"
                        )
                );

        assertEquals(
                "Debe existir al menos un umbral de renovación configurado",
                exception.getMessage()
        );
    }

    // =========================================================
    // TEST 16
    // =========================================================

    @Test
    void calcularUmbralMaximo_conUmbralesConfigurados_calculaMayorUmbral() {

        // Arrange

        RenovacionService service =
                new RenovacionService(
                        dominioRepository,
                        tokenService
                );

        ReflectionTestUtils.setField(
                service,
                "umbrales",
                List.of(1, 5, 15, 30)
        );

        // Act

        ReflectionTestUtils.invokeMethod(
                service,
                "calcularUmbralMaximo"
        );

        // Assert

        int umbralMaximo =
                (int) ReflectionTestUtils.getField(
                        service,
                        "umbralMaximo"
                );

        assertEquals(
                30,
                umbralMaximo
        );
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private Cliente crearCliente(
            String nombre,
            String email) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return cliente;
    }

    private Dominio crearDominio(
            Cliente cliente,
            LocalDate fechaExpiracion) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio("dominio-test.com");
        dominio.setFechaExpiracion(fechaExpiracion);
        dominio.setEstado(Estado.ACTIVO);

        return dominio;
    }
}