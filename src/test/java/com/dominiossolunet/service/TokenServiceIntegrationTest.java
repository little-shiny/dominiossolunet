package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import(TokenService.class)
class TokenServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;


    // ============================================================
    // marcaEstadoRenovacionPorListaIdDominio
    // ============================================================

    @Test
    void marcaEstadoRenovacion_debePersistirCambiosAunqueElTokenVengaDeUnaConsultaAnterior() {

        // ARRANGE

        Cliente cliente = crearCliente(
                "Ana",
                "ana@ana.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "ana.com"
        );

        int idDominio = dominio.getId();

        TokenCliente tokenCliente = crearToken(
                cliente,
                "abc123"
        );

        TokenDominio tokenDominio = crearTokenDominio(
                tokenCliente,
                dominio
        );

        // =====================================================
        // SIMULAR GET
        // =====================================================

        TokenCliente tokenRecuperado =
                tokenClienteRepository
                        .findByToken("abc123")
                        .orElseThrow();

        // =====================================================
        // SIMULAR CAMBIO DE PETICIÓN HTTP
        // =====================================================

        entityManager.clear();

        // =====================================================
        // POST
        // =====================================================

        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenRecuperado
                );

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(idDominio),
                resultadoValidacion
        );

        // =====================================================
        // FORZAR ESCRITURA Y NUEVA LECTURA
        // =====================================================

        entityManager.flush();
        entityManager.clear();

        // =====================================================
        // ASSERT
        // =====================================================

        TokenCliente comprobacion =
                tokenClienteRepository
                        .findByToken("abc123")
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(comprobacion);

        assertThat(tokenDominios)
                .hasSize(1);

        assertThat(tokenDominios.getFirst().getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(comprobacion.isUsado())
                .isTrue();
    }


    @Test
    void marcaEstadoRenovacionPorListaIdDominio_todosSeleccionados_confirmaTodos() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "dominio1.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "dominio2.com"
        );

        TokenCliente token = crearToken(
                cliente,
                "token-todos"
        );

        crearTokenDominio(token, dominio1);
        crearTokenDominio(token, dominio2);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        // ACT

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(
                        dominio1.getId(),
                        dominio2.getId()
                ),
                resultado
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken("token-todos")
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(2);

        assertThat(tokenDominios)
                .allMatch(td ->
                        td.getEstadoAvisoRenovacion()
                                == EstadoAvisoRenovacion.CONFIRMADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void marcaEstadoRenovacionPorListaIdDominio_algunosSeleccionados_confirmaYRechazaResto() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "dominio1.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "dominio2.com"
        );

        Dominio dominio3 = crearDominio(
                cliente,
                "dominio3.com"
        );

        TokenCliente token = crearToken(
                cliente,
                "token-algunos"
        );

        crearTokenDominio(token, dominio1);
        crearTokenDominio(token, dominio2);
        crearTokenDominio(token, dominio3);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        // ACT
        // Solo se seleccionan dominio1 y dominio3.

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(
                        dominio1.getId(),
                        dominio3.getId()
                ),
                resultado
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken("token-algunos")
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(3);

        TokenDominio td1 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio1.getId()
        );

        TokenDominio td2 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio2.getId()
        );

        TokenDominio td3 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio3.getId()
        );

        assertThat(td1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(td2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(td3.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void marcaEstadoRenovacionPorListaIdDominio_ningunoSeleccionado_rechazaTodos() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "dominio1.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "dominio2.com"
        );

        TokenCliente token = crearToken(
                cliente,
                "token-ninguno"
        );

        crearTokenDominio(token, dominio1);
        crearTokenDominio(token, dominio2);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        // ACT
        // No se selecciona ningún dominio.

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(),
                resultado
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken("token-ninguno")
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(2);

        assertThat(tokenDominios)
                .allMatch(td ->
                        td.getEstadoAvisoRenovacion()
                                == EstadoAvisoRenovacion.RECHAZADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void marcaEstadoRenovacionPorListaIdDominio_tokenNoExiste_lanzaExcepcion() {

        // ARRANGE

        TokenCliente token = new TokenCliente();
        token.setToken("token-inexistente");

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        token
                );

        // ACT & ASSERT

        assertThatThrownBy(() ->
                tokenService.marcaEstadoRenovacionPorListaIdDominio(
                        List.of(1),
                        resultado
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token no encontrado");
    }


    // ============================================================
    // procesarConfirmacion
    // ============================================================

    @Test
    void procesarConfirmacion_todosLosDominiosConfirmados_debeMarcarTodosComoConfirmadosYTokenUsado() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "ana.es"
        );

        TokenCliente token = crearToken(cliente);

        TokenDominio tokenDominio1 =
                crearTokenDominio(token, dominio1);

        TokenDominio tokenDominio2 =
                crearTokenDominio(token, dominio2);

        // ACT

        tokenService.procesarConfirmacion(
                token,
                List.of(
                        dominio1.getId(),
                        dominio2.getId()
                )
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken(token.getToken())
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(2);

        assertThat(tokenDominios)
                .allMatch(td ->
                        td.getEstadoAvisoRenovacion()
                                == EstadoAvisoRenovacion.CONFIRMADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void procesarConfirmacion_algunosDominiosConfirmados_debeConfirmarSeleccionadosYRechazarResto() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "ana.es"
        );

        Dominio dominio3 = crearDominio(
                cliente,
                "ana.net"
        );

        TokenCliente token = crearToken(cliente);

        crearTokenDominio(token, dominio1);
        crearTokenDominio(token, dominio2);
        crearTokenDominio(token, dominio3);

        // ACT
        // Se confirman dominio1 y dominio3.
        // dominio2 debe quedar rechazado.

        tokenService.procesarConfirmacion(
                token,
                List.of(
                        dominio1.getId(),
                        dominio3.getId()
                )
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken(token.getToken())
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(3);

        TokenDominio td1 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio1.getId()
        );

        TokenDominio td2 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio2.getId()
        );

        TokenDominio td3 = buscarTokenDominioPorDominio(
                tokenDominios,
                dominio3.getId()
        );

        assertThat(td1.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(td2.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.RECHAZADO);

        assertThat(td3.getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void procesarConfirmacion_ningunDominioConfirmado_debeRechazarTodosYMarcarTokenComoUsado() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                cliente,
                "ana.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "ana.es"
        );

        TokenCliente token = crearToken(cliente);

        crearTokenDominio(token, dominio1);
        crearTokenDominio(token, dominio2);

        // ACT

        tokenService.procesarConfirmacion(
                token,
                List.of()
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken(token.getToken())
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(2);

        assertThat(tokenDominios)
                .allMatch(td ->
                        td.getEstadoAvisoRenovacion()
                                == EstadoAvisoRenovacion.RECHAZADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    @Test
    void procesarConfirmacion_tokenYaUsado_debeProcesarLosDominiosYMantenerTokenComoUsado() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio = crearDominio(
                cliente,
                "ana.com"
        );

        TokenCliente token = crearToken(cliente);
        token.setUsado(true);

        token = entityManager.persistAndFlush(token);

        crearTokenDominio(token, dominio);

        // ACT

        tokenService.procesarConfirmacion(
                token,
                List.of(dominio.getId())
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        TokenCliente tokenActualizado =
                tokenClienteRepository
                        .findByToken(token.getToken())
                        .orElseThrow();

        List<TokenDominio> tokenDominios =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado);

        assertThat(tokenDominios)
                .hasSize(1);

        assertThat(tokenDominios.getFirst().getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }


    // ============================================================
    // validarToken
    // ============================================================

    @Test
    void validarToken_tokenValido_devuelveValido() {

        // ARRANGE

        crearToken(
                "token-valido",
                LocalDateTime.now().plusDays(5),
                false
        );

        // ACT

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-valido");

        // ASSERT

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.VALIDO);

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().getToken())
                .isEqualTo("token-valido");

        assertThat(resultado.token().isUsado())
                .isFalse();
    }


    @Test
    void validarToken_tokenUsado_devuelveUsado() {

        // ARRANGE

        crearToken(
                "token-usado",
                LocalDateTime.now().plusDays(5),
                true
        );

        // ACT

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-usado");

        // ASSERT

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.USADO);

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().getToken())
                .isEqualTo("token-usado");

        assertThat(resultado.token().isUsado())
                .isTrue();
    }


    @Test
    void validarToken_tokenExpirado_devuelveExpirado() {

        // ARRANGE

        crearToken(
                "token-expirado",
                LocalDateTime.now().minusDays(1),
                false
        );

        // ACT

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-expirado");

        // ASSERT

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.EXPIRADO);

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().getToken())
                .isEqualTo("token-expirado");

        assertThat(resultado.token().isUsado())
                .isFalse();
    }


    @Test
    void validarToken_tokenNoEncontrado_devuelveNoEncontrado() {

        // ACT

        ResultadoValidacionRec resultado =
                tokenService.validarToken(
                        "token-inexistente"
                );

        // ASSERT

        assertThat(resultado.resultado())
                .isEqualTo(ResultadoValidacion.NO_ENCONTRADO);

        assertThat(resultado.token())
                .isNull();
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private Cliente crearCliente() {

        return crearCliente(
                "Ana",
                "ana@ana.com"
        );
    }


    private Cliente crearCliente(
            String nombre,
            String email) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return entityManager.persistAndFlush(cliente);
    }


    private Dominio crearDominio(
            Cliente cliente,
            String nombre) {

        Dominio dominio = new Dominio();

        dominio.setNombreDominio(nombre);
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(20)
        );
        dominio.setCliente(cliente);
        dominio.setRegistrador(Registrador.DOMITECA);

        return entityManager.persistAndFlush(dominio);
    }


    private TokenCliente crearToken(
            Cliente cliente) {

        TokenCliente token = new TokenCliente();

        token.setCliente(cliente);
        token.setToken(UUID.randomUUID().toString());
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(
                LocalDateTime.now().plusDays(7)
        );
        token.setUsado(false);

        return entityManager.persistAndFlush(token);
    }


    private TokenCliente crearToken(
            Cliente cliente,
            String valorToken) {

        TokenCliente token = new TokenCliente();

        token.setCliente(cliente);
        token.setToken(valorToken);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(
                LocalDateTime.now().plusDays(5)
        );
        token.setUsado(false);

        return entityManager.persistAndFlush(token);
    }


    private TokenCliente crearToken(
            String valorToken,
            LocalDateTime fechaExpiracion,
            boolean usado) {

        Cliente cliente = crearCliente();

        TokenCliente token = new TokenCliente();

        token.setCliente(cliente);
        token.setToken(valorToken);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(fechaExpiracion);
        token.setUsado(usado);

        return entityManager.persistAndFlush(token);
    }


    private TokenDominio crearTokenDominio(
            TokenCliente token,
            Dominio dominio) {

        TokenDominio tokenDominio = new TokenDominio();

        tokenDominio.setTokenCliente(token);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        return entityManager.persistAndFlush(tokenDominio);
    }


    private TokenDominio buscarTokenDominioPorDominio(
            List<TokenDominio> tokenDominios,
            int idDominio) {

        return tokenDominios.stream()
                .filter(td ->
                        td.getDominio().getId() == idDominio
                )
                .findFirst()
                .orElseThrow();
    }
}