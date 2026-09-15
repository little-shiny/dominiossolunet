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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenClienteRepository tokenClienteRepository;

    @Mock
    private TokenDominioRepository tokenDominioRepository;

    @InjectMocks
    private TokenService tokenService;

    // TESTS validarToken()
    @Test
    void validarToken_cuandoTokenValido_devuelveResultadoValido() {

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        //Token valido
        TokenCliente token = new TokenCliente();
        token.setToken("abc123");
        token.setUsado(false);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        token.setCliente(cliente);

        when(tokenClienteRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado = tokenService.validarToken("abc123");

        assertThat(resultado.resultado()).isEqualTo(ResultadoValidacion.VALIDO);
        assertThat(resultado.token()).isEqualTo(token);
        assertThat(resultado.token().getCliente().getNombre()).isEqualTo("Ana");
    }

    // TESTS marcaEstadoRenovacionPorListaIdDominio()
    @Test
    void marcaEstadoRenovacion_cuandoDominioMarcado_loConfirma() throws Exception {

        Dominio dominioMarcado = new Dominio();
        setId(dominioMarcado, 1);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominioMarcado);
        tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.PENDIENTE);

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio));

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenCliente);

        tokenService.marcaEstadoRenovacionPorListaIdDominio(List.of(1), resultadoValidacion);

        assertThat(tokenDominio.getEstadoAvisoRenovacion()).isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);
    }

    @Test
    void marcaEstadoRenovacion_cuandoDominioNoMarcado_loRechaza() throws Exception {

        Dominio dominioNoMarcado = new Dominio();
        setId(dominioNoMarcado, 2);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominioNoMarcado);
        tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.PENDIENTE);

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of(tokenDominio));

        // El cliente NO marcó el dominio con id 2, marcó otro (id 99, que no existe en la lista)
        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenCliente);

        tokenService.marcaEstadoRenovacionPorListaIdDominio(List.of(99), resultadoValidacion);

        assertThat(tokenDominio.getEstadoAvisoRenovacion()).isEqualTo(EstadoAvisoRenovacion.RECHAZADO);
    }

    @Test
    void marcaEstadoRenovacion_marcaElTokenClienteComoUsado() {

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);

        when(tokenDominioRepository.findByTokenCliente(tokenCliente))
                .thenReturn(List.of());

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenCliente);

        tokenService.marcaEstadoRenovacionPorListaIdDominio(List.of(), resultadoValidacion);

        assertThat(tokenCliente.isUsado()).isTrue();
    }

    // Utilidad para asignar el id manualmente, ya que Dominio.setId() no existe
    // (el campo id tiene @Setter(AccessLevel.NONE) porque lo gestiona @GeneratedValue).
    // En un test unitario con objetos "sueltos" (no persistidos en BD), necesitamos
    // forzar el id por reflexión para poder simular que un dominio "ya tiene id X".
    private void setId(Dominio dominio, int id) throws Exception {
        Field idField = Dominio.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(dominio, id);
    }

    @Test
    void generarToken_creaTokenClienteCorrectamente() {

        // Arrange
        Cliente cliente = new Cliente();

        Dominio dominio1 = new Dominio();
        dominio1.setCliente(cliente);

        Dominio dominio2 = new Dominio();
        dominio2.setCliente(cliente);

        TokenCliente tokenGuardado = new TokenCliente();

        when(tokenClienteRepository.save(any(TokenCliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenCliente resultado = tokenService.generarToken(
                cliente,
                List.of(dominio1, dominio2)
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertFalse(resultado.isUsado());
        assertNotNull(resultado.getToken());
        assertNotNull(resultado.getFechaCreacion());
        assertNotNull(resultado.getFechaExpiracion());

        verify(tokenClienteRepository).save(any(TokenCliente.class));
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
        TokenCliente resultado = tokenService.generarToken(
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

        List<TokenDominio> tokensDominio = captor.getAllValues();

        assertEquals(resultado, tokensDominio.get(0).getTokenCliente());
        assertEquals(resultado, tokensDominio.get(1).getTokenCliente());

        assertEquals(dominio1, tokensDominio.get(0).getDominio());
        assertEquals(dominio2, tokensDominio.get(1).getDominio());
    }
}