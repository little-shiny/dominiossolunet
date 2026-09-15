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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración de TokenService.
 *
 * Comprueba que los cambios realizados sobre TokenCliente y TokenDominio
 * se persisten correctamente en una base de datos H2 real.
 */
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

    @Test
    void marcaEstadoRenovacion_debePersistirCambiosAunqueElTokenVengaDeUnaConsultaAnterior() {

        // =====================================================
        // ARRANGE
        // =====================================================

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");
        entityManager.persistAndFlush(cliente);

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setFechaExpiracion(LocalDate.now().plusDays(20));
        dominio.setCliente(cliente);
        dominio.setRegistrador(Registrador.DOMITECA);
        entityManager.persistAndFlush(dominio);

        int idDominio = dominio.getId();

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);
        tokenCliente.setFechaCreacion(LocalDateTime.now());
        tokenCliente.setFechaExpiracion(
                LocalDateTime.now().plusDays(1)
        );
        tokenCliente.setCliente(cliente);
        entityManager.persistAndFlush(tokenCliente);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );
        entityManager.persistAndFlush(tokenDominio);

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

        TokenDominio tokenDominioComprobacion =
                tokenDominioRepository
                        .findByTokenCliente(comprobacion)
                        .getFirst();

        assertThat(tokenDominioComprobacion
                .getEstadoAvisoRenovacion())
                .isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);

        assertThat(comprobacion.isUsado())
                .isTrue();
    }

    @Test
    void marcaEstadoRenovacion_siNoMarcaNingunDominio_debeRechazarTodos() {

        // =====================================================
        // ARRANGE
        // =====================================================

        Cliente cliente = new Cliente();
        cliente.setNombre("Laura");
        cliente.setEmail("laura@laura.com");
        entityManager.persistAndFlush(cliente);

        Dominio dominio1 = crearDominio(
                cliente,
                "laura1.com"
        );

        Dominio dominio2 = crearDominio(
                cliente,
                "laura2.com"
        );

        entityManager.persistAndFlush(dominio1);
        entityManager.persistAndFlush(dominio2);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("token-rechazo");
        tokenCliente.setUsado(false);
        tokenCliente.setFechaCreacion(LocalDateTime.now());
        tokenCliente.setFechaExpiracion(
                LocalDateTime.now().plusDays(1)
        );
        tokenCliente.setCliente(cliente);
        entityManager.persistAndFlush(tokenCliente);

        TokenDominio tokenDominio1 = crearTokenDominio(
                tokenCliente,
                dominio1
        );

        TokenDominio tokenDominio2 = crearTokenDominio(
                tokenCliente,
                dominio2
        );

        entityManager.persistAndFlush(tokenDominio1);
        entityManager.persistAndFlush(tokenDominio2);

        TokenCliente tokenRecuperado =
                tokenClienteRepository
                        .findByToken("token-rechazo")
                        .orElseThrow();

        entityManager.clear();

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenRecuperado
                );

        // =====================================================
        // ACT
        // =====================================================

        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(),
                resultado
        );

        entityManager.flush();
        entityManager.clear();

        // =====================================================
        // ASSERT
        // =====================================================

        TokenCliente comprobacion =
                tokenClienteRepository
                        .findByToken("token-rechazo")
                        .orElseThrow();

        List < TokenDominio > dominios =
                tokenDominioRepository
                        .findByTokenCliente(comprobacion);

        assertThat(dominios)
                .hasSize(2);

        assertThat(dominios)
                .allMatch(td ->
                        td.getEstadoAvisoRenovacion() ==
                        EstadoAvisoRenovacion.RECHAZADO
                );

        assertThat(comprobacion.isUsado())
                .isTrue();
    }

    private Dominio crearDominio(
            Cliente cliente,
            String nombre) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio(nombre);
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(20)
        );
        dominio.setRegistrador(Registrador.DOMITECA);

        return dominio;
    }

    private TokenDominio crearTokenDominio(
            TokenCliente tokenCliente,
            Dominio dominio) {

        TokenDominio tokenDominio = new TokenDominio();

        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        return tokenDominio;
    }
}