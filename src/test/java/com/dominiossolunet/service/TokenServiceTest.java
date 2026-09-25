package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.*;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.HistorialDominioRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenClienteRepository tokenClienteRepository;

    @Mock
    private TokenDominioRepository tokenDominioRepository;

    @Mock
    private HistorialDominioRepository historialDominioRepository;

    @InjectMocks
    private TokenService tokenService;

    // ============================================================
    // UTILIDADES
    // ============================================================

    private void setId(Dominio dominio, int id) throws Exception {
        Field idField = Dominio.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(dominio, id);
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");
        return cliente;
    }

    private Dominio crearDominio(
            Cliente cliente,
            int id,
            String nombre
    ) throws Exception {

        Dominio dominio = new Dominio();

        setId(dominio, id);

        dominio.setNombreDominio(nombre);
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setCliente(cliente);

        return dominio;
    }

    private TokenCliente crearToken(String token) {
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);
        tokenCliente.setUsado(false);
        return tokenCliente;
    }

    private TokenDominio crearTokenDominio(
            TokenCliente token,
            Dominio dominio
    ) {
        TokenDominio tokenDominio = new TokenDominio();

        tokenDominio.setTokenCliente(token);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        return tokenDominio;
    }

    // ============================================================
    // generarToken()
    // ============================================================

    @Test
    void generarToken_creaTokenClienteCorrectamente() {

        Cliente cliente = new Cliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of()
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCliente()).isSameAs(cliente);
        assertThat(resultado.isUsado()).isFalse();
        assertThat(resultado.getToken()).isNotNull();
        assertThat(resultado.getFechaCreacion()).isNotNull();
        assertThat(resultado.getFechaExpiracion()).isNotNull();

        verify(tokenClienteRepository).save(any(TokenCliente.class));
    }

    @Test
    void generarToken_estableceFechaExpiracionCorrectamente()
            throws Exception {

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Field field = TokenService.class
                .getDeclaredField("diasExpiracionToken");

        field.setAccessible(true);
        field.set(tokenService, 7);

        LocalDateTime antes = LocalDateTime.now();

        TokenCliente resultado = tokenService.generarToken(
                new Cliente(),
                List.of()
        );

        LocalDateTime despues = LocalDateTime.now();

        assertThat(resultado.getFechaCreacion())
                .isBetween(antes, despues);

        assertThat(resultado.getFechaExpiracion())
                .isEqualTo(resultado.getFechaCreacion().plusDays(7));
    }

    @Test
    void generarToken_creaUnTokenDominioPorCadaDominio() {

        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        verify(tokenDominioRepository, times(2))
                .save(any(TokenDominio.class));
    }

    @Test
    void generarToken_asociaCadaDominioAlTokenYQuedaPendiente() {

        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        ArgumentCaptor<TokenDominio> captor =
                ArgumentCaptor.forClass(TokenDominio.class);

        verify(tokenDominioRepository, times(2))
                .save(captor.capture());

        List<TokenDominio> tokensDominio =
                captor.getAllValues();

        assertThat(tokensDominio)
                .extracting(TokenDominio::getTokenCliente)
                .containsOnly(resultado);

        assertThat(tokensDominio)
                .extracting(TokenDominio::getDominio)
                .containsExactlyInAnyOrder(dominio1, dominio2);

        assertThat(tokensDominio)
                .extracting(TokenDominio::getEstadoAvisoRenovacion)
                .containsOnly(EstadoAvisoRenovacion.PENDIENTE);
    }

    @Test
    void generarToken_sinDominios_noCreaTokenDominio() {

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenCliente resultado = tokenService.generarToken(
                new Cliente(),
                List.of()
        );

        assertThat(resultado).isNotNull();

        verify(tokenClienteRepository).save(any(TokenCliente.class));

        verify(tokenDominioRepository, never())
                .save(any(TokenDominio.class));
    }

    // ============================================================
    // validarToken()
    // ============================================================

    @Test
    void validarToken_cuandoTokenValido_devuelveValido() {

        Cliente cliente = crearCliente();

        TokenCliente token = new TokenCliente();
        token.setToken("abc123");
        token.setUsado(false);
        token.setFechaExpiracion(
                LocalDateTime.now().plusDays(1)
        );
        token.setCliente(cliente);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado =
                tokenService.validarToken("abc123");

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.VALIDO);

        assertThat(resultado.token())
                .isSameAs(token);

        verify(tokenClienteRepository)
                .findByToken("abc123");
    }

    @Test
    void validarToken_cuandoTokenUsado_devuelveUsado() {

        TokenCliente token = new TokenCliente();
        token.setToken("token-usado");
        token.setUsado(true);
        token.setFechaExpiracion(
                LocalDateTime.now().plusDays(1)
        );

        when(tokenClienteRepository.findByToken("token-usado"))
                .thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-usado");

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.USADO);

        assertThat(resultado.token())
                .isSameAs(token);
    }

    @Test
    void validarToken_cuandoTokenExpirado_devuelveExpirado() {

        TokenCliente token = new TokenCliente();
        token.setToken("token-expirado");
        token.setUsado(false);
        token.setFechaExpiracion(
                LocalDateTime.now().minusMinutes(1)
        );

        when(tokenClienteRepository.findByToken("token-expirado"))
                .thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-expirado");

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.EXPIRADO);

        assertThat(resultado.token())
                .isSameAs(token);
    }

    @Test
    void validarToken_cuandoTokenNoExiste_devuelveNoEncontrado() {

        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        ResultadoValidacionRec resultado =
                tokenService.validarToken("inexistente");

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.NO_ENCONTRADO);

        assertThat(resultado.token()).isNull();
    }

    @Test
    void validarToken_cuandoTokenUsadoYExpirado_devuelveUsado() {

        TokenCliente token = new TokenCliente();
        token.setToken("token");
        token.setUsado(true);
        token.setFechaExpiracion(
                LocalDateTime.now().minusDays(1)
        );

        when(tokenClienteRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token");

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.USADO);
    }

    // ============================================================
    // marcarComoUsado()
    // ============================================================

    @Test
    void marcarComoUsado_marcaTokenComoUsado() {

        TokenCliente token = crearToken("abc123");

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        tokenService.marcarComoUsado("abc123");

        assertThat(token.isUsado()).isTrue();

        verify(tokenClienteRepository)
                .findByToken("abc123");

        // No se llama a save porque el método utiliza
        // dirty checking dentro de la transacción.
        verify(tokenClienteRepository, never())
                .save(any(TokenCliente.class));
    }

    @Test
    void marcarComoUsado_cuandoTokenNoExiste_lanzaExcepcion() {

        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.marcarComoUsado(
                                "inexistente"
                        )
                );

        assertThat(excepcion.getMessage())
                .isEqualTo("Token no encontrado: inexistente");
    }

    // ============================================================
    // marcaEstadoRenovacionPorListaIdDominio()
    // ============================================================

    @Test
    void marcaEstadoRenovacion_cuandoDominioMarcado_loConfirma()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio = crearDominio(
                cliente,
                1,
                "ana.com"
        );

        TokenCliente token = crearToken("abc123");

        TokenDominio tokenDominio =
                crearTokenDominio(token, dominio);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(tokenDominio));

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(1),
                resultado
        );

        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(token.isUsado()).isTrue();
    }

    @Test
    void marcaEstadoRenovacion_cuandoDominioNoMarcado_loRechaza()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio = crearDominio(
                cliente,
                2,
                "ana.com"
        );

        TokenCliente token = crearToken("abc123");

        TokenDominio tokenDominio =
                crearTokenDominio(token, dominio);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(tokenDominio));

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(99),
                resultado
        );

        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(token.isUsado()).isTrue();
    }

    @Test
    void marcaEstadoRenovacion_conVariosDominios_marcaCadaUnoSegunSeleccion()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                1,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                2,
                "ana.es"
        );

        Dominio dominio3 = crearDominio(
                cliente,
                3,
                "ana.net"
        );

        TokenCliente token = crearToken("abc123");

        TokenDominio td1 =
                crearTokenDominio(token, dominio1);

        TokenDominio td2 =
                crearTokenDominio(token, dominio2);

        TokenDominio td3 =
                crearTokenDominio(token, dominio3);

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(td1, td2, td3));

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(1, 3),
                resultado
        );

        assertThat(td1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(td2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(td3.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(token.isUsado()).isTrue();
    }

    @Test
    void marcaEstadoRenovacion_sinDominios_marcaTokenComoUsado() {

        TokenCliente token = crearToken("abc123");

        when(tokenClienteRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of());

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(),
                resultado
        );

        assertThat(token.isUsado()).isTrue();
    }

    @Test
    void marcaEstadoRenovacion_cuandoTokenNoExiste_lanzaExcepcion() {

        TokenCliente token = crearToken("inexistente");

        when(tokenClienteRepository.findByToken("inexistente"))
                .thenReturn(Optional.empty());

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tokenService.marcaEstadoRenovacionPorListaIdDominio(
                                List.of(),
                                resultado
                        )
                );

        assertThat(excepcion.getMessage())
                .isEqualTo("Token no encontrado: inexistente");

        verify(tokenDominioRepository, never())
                .findByTokenCliente(any());
    }

    // ============================================================
    // procesarConfirmacion()
    // ============================================================

    @Test
    void procesarConfirmacion_todosConfirmados_registraHistorial()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                1,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                2,
                "ana.es"
        );

        TokenCliente token = crearToken("token123");
        token.setCliente(cliente);

        TokenDominio tokenDominio1 =
                crearTokenDominio(token, dominio1);

        TokenDominio tokenDominio2 =
                crearTokenDominio(token, dominio2);

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(
                        tokenDominio1,
                        tokenDominio2
                ));

        when(tokenClienteRepository.findByToken("token123"))
                .thenReturn(Optional.of(token));

        tokenService.procesarConfirmacion(
                token,
                List.of(
                        dominio1.getId(),
                        dominio2.getId()
                )
        );

        assertThat(tokenDominio1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenDominio2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenDominio1.getFechaInteraccionCliente())
                .isNotNull();

        assertThat(tokenDominio2.getFechaInteraccionCliente())
                .isNotNull();

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository, times(2))
                .save(captor.capture());

        List<HistorialDominio> historiales =
                captor.getAllValues();

        assertThat(historiales)
                .allMatch(historial ->
                        historial.getTipoEvento()
                                == TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION
                );

        assertThat(historiales)
                .extracting(HistorialDominio::getDominio)
                .containsExactlyInAnyOrder(
                        dominio1,
                        dominio2
                );

        assertThat(token.isUsado()).isTrue();

        // La aceptación del cliente no renueva el dominio.
        assertThat(dominio1.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominio2.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);
    }

    @Test
    void procesarConfirmacion_algunosConfirmados_registraHistorialCorrectamente()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                1,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                2,
                "ana.es"
        );

        TokenCliente token = crearToken("token123");
        token.setCliente(cliente);

        TokenDominio tokenDominio1 =
                crearTokenDominio(token, dominio1);

        TokenDominio tokenDominio2 =
                crearTokenDominio(token, dominio2);

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(
                        tokenDominio1,
                        tokenDominio2
                ));

        when(tokenClienteRepository.findByToken("token123"))
                .thenReturn(Optional.of(token));

        // El cliente acepta únicamente ana.com.
        tokenService.procesarConfirmacion(
                token,
                List.of(dominio1.getId())
        );

        assertThat(tokenDominio1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenDominio2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(tokenDominio1.getFechaInteraccionCliente())
                .isNotNull();

        assertThat(tokenDominio2.getFechaInteraccionCliente())
                .isNotNull();

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository, times(2))
                .save(captor.capture());

        List<HistorialDominio> historiales =
                captor.getAllValues();

        assertThat(historiales)
                .anyMatch(historial ->
                        historial.getDominio() == dominio1 &&
                                historial.getTipoEvento()
                                        == TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION
                );

        assertThat(historiales)
                .anyMatch(historial ->
                        historial.getDominio() == dominio2 &&
                                historial.getTipoEvento()
                                        == TipoEventoDominio.CLIENTE_RECHAZA_RENOVACION
                );

        assertThat(token.isUsado()).isTrue();
    }

    @Test
    void procesarConfirmacion_ningunoConfirmado_registraRechazos()
            throws Exception {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                1,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                2,
                "ana.es"
        );

        TokenCliente token = crearToken("token123");
        token.setCliente(cliente);

        TokenDominio tokenDominio1 =
                crearTokenDominio(token, dominio1);

        TokenDominio tokenDominio2 =
                crearTokenDominio(token, dominio2);

        when(tokenDominioRepository.findByTokenCliente(token))
                .thenReturn(List.of(
                        tokenDominio1,
                        tokenDominio2
                ));

        when(tokenClienteRepository.findByToken("token123"))
                .thenReturn(Optional.of(token));

        tokenService.procesarConfirmacion(
                token,
                List.of()
        );

        assertThat(tokenDominio1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(tokenDominio2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(tokenDominio1.getFechaInteraccionCliente())
                .isNotNull();

        assertThat(tokenDominio2.getFechaInteraccionCliente())
                .isNotNull();

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository, times(2))
                .save(captor.capture());

        assertThat(captor.getAllValues())
                .allMatch(historial ->
                        historial.getTipoEvento()
                                == TipoEventoDominio.CLIENTE_RECHAZA_RENOVACION
                );

        assertThat(token.isUsado()).isTrue();
    }
}