package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TokenService.
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenClienteRepository tokenClienteRepository;

    @Mock
    private TokenDominioRepository tokenDominioRepository;

    @InjectMocks
    private TokenService tokenService;

    // ============================================================
    // UTILIDAD PARA ASIGNAR ID A DOMINIO
    // ============================================================

    /*
     * Dominio.setId() no existe porque el campo id tiene
     * @Setter(AccessLevel.NONE).
     *
     * En estos tests unitarios los objetos no están persistidos
     * en BD, por lo que usamos reflexión para simular un ID.
     */
    private void setId(Dominio dominio, int id) throws Exception {
        Field idField = Dominio.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(dominio, id);
    }

    // ============================================================
    // TESTS generarToken()
    // ============================================================

    @Test
    void generarToken_creaTokenClienteCorrectamente() {

        // Arrange
        Cliente cliente = new Cliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of()
        );

        // Assert
        assertNotNull(resultado);

        assertEquals(cliente, resultado.getCliente());

        assertFalse(resultado.isUsado());

        assertNotNull(resultado.getToken());

        assertNotNull(resultado.getFechaCreacion());

        assertNotNull(resultado.getFechaExpiracion());

        verify(tokenClienteRepository)
                .save(any(TokenCliente.class));
    }

    @Test
    void generarToken_generaUUIDValido() {

        // Arrange
        Cliente cliente = new Cliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of()
        );

        // Assert
        assertNotNull(resultado.getToken());

        assertDoesNotThrow(() ->
                java.util.UUID.fromString(resultado.getToken())
        );
    }

    @Test
    void generarToken_estableceFechaCreacionYExpiracionCorrectamente() throws Exception {

        // Arrange
        Cliente cliente = new Cliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Configuramos la duración del token
        Field field = TokenService.class
                .getDeclaredField("diasExpiracionToken");

        field.setAccessible(true);
        field.set(tokenService, 7);

        LocalDateTime antes = LocalDateTime.now();

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of()
        );

        LocalDateTime despues = LocalDateTime.now();

        // Assert
        assertNotNull(resultado.getFechaCreacion());
        assertNotNull(resultado.getFechaExpiracion());

        assertFalse(
                resultado.getFechaCreacion().isBefore(antes)
        );

        assertFalse(
                resultado.getFechaCreacion().isAfter(despues)
        );

        assertEquals(
                resultado.getFechaCreacion().plusDays(7),
                resultado.getFechaExpiracion()
        );
    }

    @Test
    void generarToken_creaUnTokenDominioPorCadaDominio() {

        // Arrange
        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        // Assert
        verify(tokenDominioRepository, times(2))
                .save(any(TokenDominio.class));
    }

    @Test
    void generarToken_asociaCadaDominioAlTokenCliente() {

        // Arrange
        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        // Assert
        ArgumentCaptor<TokenDominio> captor =
                ArgumentCaptor.forClass(TokenDominio.class);

        verify(tokenDominioRepository, times(2))
                .save(captor.capture());

        List<TokenDominio> tokensDominio =
                captor.getAllValues();

        assertEquals(
                resultado,
                tokensDominio.get(0).getTokenCliente()
        );

        assertEquals(
                resultado,
                tokensDominio.get(1).getTokenCliente()
        );

        assertEquals(
                dominio1,
                tokensDominio.get(0).getDominio()
        );

        assertEquals(
                dominio2,
                tokensDominio.get(1).getDominio()
        );
    }

    @Test
    void generarToken_estableceEstadoPendienteEnCadaTokenDominio() {

        // Arrange
        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        // Assert
        ArgumentCaptor<TokenDominio> captor =
                ArgumentCaptor.forClass(TokenDominio.class);

        verify(tokenDominioRepository, times(2))
                .save(captor.capture());

        for (TokenDominio tokenDominio : captor.getAllValues()) {
            assertEquals(
                    EstadoAvisoRenovacion.PENDIENTE,
                    tokenDominio.getEstadoAvisoRenovacion()
            );
        }
    }

    @Test
    void generarToken_sinDominios_noCreaTokenDominio() {

        // Arrange
        Cliente cliente = new Cliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of()
        );

        // Assert
        assertNotNull(resultado);

        verify(tokenClienteRepository).save(any(TokenCliente.class));

        verify(tokenDominioRepository, never())
                .save(any(TokenDominio.class));
    }

    // ============================================================
    // TESTS validarToken()
    // ============================================================

    @Test
    void validarToken_cuandoTokenValido_devuelveResultadoValido() {

        // Arrange
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        TokenCliente token = new TokenCliente();
        token.setToken("abc123");
        token.setUsado(false);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        token.setCliente(cliente);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        // Act
        ResultadoValidacionRec resultado =
                tokenService.validarToken("abc123");

        // Assert
        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.VALIDO);

        assertThat(resultado.token())
                .isEqualTo(token);

        assertThat(resultado.token().getCliente().getNombre())
                .isEqualTo("Ana");

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }

    @Test
    void validarToken_cuandoTokenUsado_devuelveResultadoUsado() {

        // Arrange
        TokenCliente token = new TokenCliente();
        token.setToken("token-usado");
        token.setUsado(true);
        token.setFechaExpiracion(
                LocalDateTime.now().plusDays(1)
        );

        when(tokenClienteRepository.findByToken("token-usado"))
                .thenReturn(Optional.of(token));

        // Act
        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-usado");

        // Assert
        assertEquals(
                ResultadoValidacion.USADO,
                resultado.resultado()
        );

        assertEquals(
                token,
                resultado.token()
        );

        verify(tokenClienteRepository)
                .findByToken("token-usado");
    }

    @Test
    void validarToken_cuandoTokenExpirado_devuelveResultadoExpirado() {

        // Arrange
        TokenCliente token = new TokenCliente();
        token.setToken("token-expirado");
        token.setUsado(false);
        token.setFechaExpiracion(
                LocalDateTime.now().minusMinutes(1)
        );

        when(tokenClienteRepository.findByToken("token-expirado"))
                .thenReturn(Optional.of(token));

        // Act
        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-expirado");

        // Assert
        assertEquals(
                ResultadoValidacion.EXPIRADO,
                resultado.resultado()
        );

        assertEquals(
                token,
                resultado.token()
        );

        verify(tokenClienteRepository)
                .findByToken("token-expirado");
    }

    @Test
    void validarToken_cuandoTokenNoExiste_devuelveResultadoNoEncontrado() {

        // Arrange
        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        // Act
        ResultadoValidacionRec resultado =
                tokenService.validarToken("inexistente");

        // Assert
        assertEquals(
                ResultadoValidacion.NO_ENCONTRADO,
                resultado.resultado()
        );

        assertNull(resultado.token());

        verify(tokenClienteRepository)
                .findByToken("inexistente");
    }

    @Test
    void validarToken_cuandoTokenUsadoYExpirado_devuelveUsado() {

        // Arrange
        TokenCliente token = new TokenCliente();
        token.setToken("token");
        token.setUsado(true);
        token.setFechaExpiracion(
                LocalDateTime.now().minusDays(1)
        );

        when(tokenClienteRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        // Act
        ResultadoValidacionRec resultado =
                tokenService.validarToken("token");

        // Assert
        /*
         * El servicio comprueba primero si está usado,
         * por lo que USADO tiene prioridad sobre EXPIRADO.
         */
        assertEquals(
                ResultadoValidacion.USADO,
                resultado.resultado()
        );
    }

    // ============================================================
    // TESTS marcarComoUsado()
    // ============================================================

    @Test
    void marcarComoUsado_marcaTokenComoUsado() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        // Act
        tokenService.marcarComoUsado("abc123");

        // Assert
        assertTrue(tokenCliente.isUsado());

        verify(tokenClienteRepository)
                .findByToken("abc123");

        /*
         * El servicio utiliza dirty checking,
         * por lo que no debe llamar a save().
         */
        verify(tokenClienteRepository, never())
                .save(any(TokenCliente.class));
    }

    @Test
    void marcarComoUsado_cuandoTokenNoExiste_lanzaExcepcion() {

        // Arrange
        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.marcarComoUsado("inexistente")
                );

        assertEquals(
                "Token no encontrado: inexistente",
                excepcion.getMessage()
        );

        verify(tokenClienteRepository)
                .findByToken("inexistente");
    }

    // ============================================================
    // TESTS obtenerTokenDominioPorTokenCliente()
    // ============================================================

    @Test
    void obtenerTokenDominioPorTokenCliente_devuelveTokenDominios() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();

        TokenDominio tokenDominio1 = new TokenDominio();
        TokenDominio tokenDominio2 = new TokenDominio();

        List<TokenDominio> esperados =
                List.of(tokenDominio1, tokenDominio2);

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(esperados);

        // Act
        List<TokenDominio> resultado =
                tokenService.obtenerTokenDominioPorTokenCliente(tokenCliente);

        // Assert
        assertEquals(
                esperados,
                resultado
        );

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);
    }

    // ============================================================
    // TESTS marcaEstadoRenovacionPorListaIdDominio()
    // ============================================================

    @Test
    void marcaEstadoRenovacion_cuandoDominioMarcado_loConfirma()
            throws Exception {

        // Arrange
        Dominio dominioMarcado = new Dominio();
        setId(dominioMarcado, 1);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominioMarcado);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio));

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        // Act
        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(1),
                resultadoValidacion
        );

        // Assert
        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertTrue(tokenCliente.isUsado());

        verify(tokenClienteRepository)
                .findByToken("abc123");

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);
    }

    @Test
    void marcaEstadoRenovacion_cuandoDominioNoMarcado_loRechaza()
            throws Exception {

        // Arrange
        Dominio dominioNoMarcado = new Dominio();
        setId(dominioNoMarcado, 2);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominioNoMarcado);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio));

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        // Act
        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(99),
                resultadoValidacion
        );

        // Assert
        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertTrue(tokenCliente.isUsado());

        verify(tokenClienteRepository)
                .findByToken("abc123");

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);
    }

    @Test
    void marcaEstadoRenovacion_conVariosDominios_marcaCadaUnoSegunSeleccion()
            throws Exception {

        // Arrange
        Dominio dominio1 = new Dominio();
        Dominio dominio2 = new Dominio();
        Dominio dominio3 = new Dominio();

        setId(dominio1, 1);
        setId(dominio2, 2);
        setId(dominio3, 3);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        TokenDominio td1 = new TokenDominio();
        td1.setTokenCliente(tokenCliente);
        td1.setDominio(dominio1);
        td1.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio td2 = new TokenDominio();
        td2.setTokenCliente(tokenCliente);
        td2.setDominio(dominio2);
        td2.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio td3 = new TokenDominio();
        td3.setTokenCliente(tokenCliente);
        td3.setDominio(dominio3);
        td3.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(td1, td2, td3));

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        // Act
        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(1, 3),
                resultadoValidacion
        );

        // Assert
        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                td1.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.RECHAZADO,
                td2.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                td3.getEstadoAvisoRenovacion()
        );

        assertTrue(tokenCliente.isUsado());
    }

    @Test
    void marcaEstadoRenovacion_sinDominios_marcaTokenComoUsado()
            throws Exception {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of());

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        // Act
        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(),
                resultadoValidacion
        );

        // Assert
        assertTrue(tokenCliente.isUsado());

        verify(tokenClienteRepository)
                .findByToken("abc123");

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);
    }

    @Test
    void marcaEstadoRenovacion_cuandoTokenNoExiste_lanzaExcepcion()
            throws Exception {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("inexistente");

        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        // Act + Assert
        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.marcaEstadoRenovacionPorListaIdDominio(
                                List.of(),
                                resultadoValidacion
                        )
                );

        assertEquals(
                "Token no encontrado: inexistente",
                excepcion.getMessage()
        );

        verify(tokenClienteRepository)
                .findByToken("inexistente");

        verify(tokenDominioRepository, never())
                .findByTokenCliente(any());
    }


// ============================================================
// TESTS procesarConfirmacion()
// ============================================================

    @Test
    void procesarConfirmacion_todosLosDominiosConfirmados_marcaTodosComoConfirmadosYTokenUsado() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        Dominio dominio1 = mock(Dominio.class);
        Dominio dominio2 = mock(Dominio.class);

        when(dominio1.getId()).thenReturn(1);
        when(dominio2.getId()).thenReturn(2);

        TokenDominio tokenDominio1 = new TokenDominio();
        tokenDominio1.setTokenCliente(tokenCliente);
        tokenDominio1.setDominio(dominio1);
        tokenDominio1.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio tokenDominio2 = new TokenDominio();
        tokenDominio2.setTokenCliente(tokenCliente);
        tokenDominio2.setDominio(dominio2);
        tokenDominio2.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio1, tokenDominio2));

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        // Act
        tokenService.procesarConfirmacion(
                tokenCliente,
                List.of(1, 2)
        );

        // Assert
        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                tokenDominio1.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                tokenDominio2.getEstadoAvisoRenovacion()
        );

        assertTrue(tokenCliente.isUsado());

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }


    @Test
    void procesarConfirmacion_algunosDominiosConfirmados_marcaCorrectamenteCadaEstadoYTokenUsado() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        Dominio dominio1 = mock(Dominio.class);
        Dominio dominio2 = mock(Dominio.class);
        Dominio dominio3 = mock(Dominio.class);

        when(dominio1.getId()).thenReturn(1);
        when(dominio2.getId()).thenReturn(2);
        when(dominio3.getId()).thenReturn(3);

        TokenDominio tokenDominio1 = new TokenDominio();
        tokenDominio1.setTokenCliente(tokenCliente);
        tokenDominio1.setDominio(dominio1);
        tokenDominio1.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio tokenDominio2 = new TokenDominio();
        tokenDominio2.setTokenCliente(tokenCliente);
        tokenDominio2.setDominio(dominio2);
        tokenDominio2.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio tokenDominio3 = new TokenDominio();
        tokenDominio3.setTokenCliente(tokenCliente);
        tokenDominio3.setDominio(dominio3);
        tokenDominio3.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(
                        tokenDominio1,
                        tokenDominio2,
                        tokenDominio3
                ));

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        // Act
        tokenService.procesarConfirmacion(
                tokenCliente,
                List.of(1, 3)
        );

        // Assert
        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                tokenDominio1.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.RECHAZADO,
                tokenDominio2.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.CONFIRMADO,
                tokenDominio3.getEstadoAvisoRenovacion()
        );

        assertTrue(tokenCliente.isUsado());

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }


    @Test
    void procesarConfirmacion_ningunDominioConfirmado_marcaTodosComoRechazadosYTokenUsado() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        Dominio dominio1 = mock(Dominio.class);
        Dominio dominio2 = mock(Dominio.class);

        when(dominio1.getId()).thenReturn(1);
        when(dominio2.getId()).thenReturn(2);

        TokenDominio tokenDominio1 = new TokenDominio();
        tokenDominio1.setTokenCliente(tokenCliente);
        tokenDominio1.setDominio(dominio1);
        tokenDominio1.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        TokenDominio tokenDominio2 = new TokenDominio();
        tokenDominio2.setTokenCliente(tokenCliente);
        tokenDominio2.setDominio(dominio2);
        tokenDominio2.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio1, tokenDominio2));

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        // Act
        tokenService.procesarConfirmacion(
                tokenCliente,
                List.of()
        );

        // Assert
        assertEquals(
                EstadoAvisoRenovacion.RECHAZADO,
                tokenDominio1.getEstadoAvisoRenovacion()
        );

        assertEquals(
                EstadoAvisoRenovacion.RECHAZADO,
                tokenDominio2.getEstadoAvisoRenovacion()
        );

        assertTrue(tokenCliente.isUsado());

        verify(tokenDominioRepository)
                .findByTokenCliente(tokenCliente);

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }


    @Test
    void procesarConfirmacion_marcaTokenComoUsado() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of());

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(tokenCliente));

        // Act
        tokenService.procesarConfirmacion(
                tokenCliente,
                List.of()
        );

        // Assert
        assertTrue(tokenCliente.isUsado());

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }

}
