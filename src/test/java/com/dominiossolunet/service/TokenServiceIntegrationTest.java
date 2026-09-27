package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
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

    @Autowired
    private HistorialDominioRepository historialDominioRepository;

    // ============================================================
    // procesarConfirmacion()
    // ============================================================

    @Test
    void procesarConfirmacion_todosLosDominiosConfirmados_debePersistirLosCambios() {

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

        Dominio dominio1Actualizado =
                dominioRepository
                        .findById(dominio1.getId())
                        .orElseThrow();

        Dominio dominio2Actualizado =
                dominioRepository
                        .findById(dominio2.getId())
                        .orElseThrow();

        assertThat(dominio1Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.PENDIENTE_RENOVACION
                );

        assertThat(dominio2Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.PENDIENTE_RENOVACION
                );
    }

    @Test
    void procesarConfirmacion_algunosDominiosConfirmados_debeConfirmarUnosYRechazarOtros() {

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

        TokenDominio td1 =
                buscarTokenDominioPorDominio(
                        tokenDominios,
                        dominio1.getId()
                );

        TokenDominio td2 =
                buscarTokenDominioPorDominio(
                        tokenDominios,
                        dominio2.getId()
                );

        TokenDominio td3 =
                buscarTokenDominioPorDominio(
                        tokenDominios,
                        dominio3.getId()
                );

        assertThat(td1.getEstadoAvisoRenovacion())
                .isEqualTo(
                        EstadoAvisoRenovacion.CONFIRMADO
                );

        assertThat(td2.getEstadoAvisoRenovacion())
                .isEqualTo(
                        EstadoAvisoRenovacion.RECHAZADO
                );

        assertThat(td3.getEstadoAvisoRenovacion())
                .isEqualTo(
                        EstadoAvisoRenovacion.CONFIRMADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();

        Dominio dominio1Actualizado =
                dominioRepository
                        .findById(dominio1.getId())
                        .orElseThrow();

        Dominio dominio2Actualizado =
                dominioRepository
                        .findById(dominio2.getId())
                        .orElseThrow();

        Dominio dominio3Actualizado =
                dominioRepository
                        .findById(dominio3.getId())
                        .orElseThrow();

        assertThat(dominio1Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.PENDIENTE_RENOVACION
                );

        assertThat(dominio2Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.RECHAZADO
                );

        assertThat(dominio3Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.PENDIENTE_RENOVACION
                );
    }

    @Test
    void procesarConfirmacion_ningunDominioConfirmado_debeRechazarTodos() {

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

        Dominio dominio1Actualizado =
                dominioRepository
                        .findById(dominio1.getId())
                        .orElseThrow();

        Dominio dominio2Actualizado =
                dominioRepository
                        .findById(dominio2.getId())
                        .orElseThrow();

        assertThat(dominio1Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.RECHAZADO
                );

        assertThat(dominio2Actualizado.getEstadoRenovacion())
                .isEqualTo(
                        EstadoRenovacion.RECHAZADO
                );
    }

    @Test
    void procesarConfirmacion_registraHistorialParaCadaDominio() {

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
                List.of(dominio1.getId())
        );

        // ASSERT

        entityManager.flush();
        entityManager.clear();

        List<HistorialDominio> historial1 =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioRepository
                                        .findById(dominio1.getId())
                                        .orElseThrow()
                        );

        List<HistorialDominio> historial2 =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioRepository
                                        .findById(dominio2.getId())
                                        .orElseThrow()
                        );

        assertThat(historial1)
                .hasSize(1);

        assertThat(historial1.getFirst().getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION
                );

        assertThat(historial1.getFirst().getDetalle())
                .isEqualTo(
                        "El cliente ha aceptado la renovación del dominio"
                );

        assertThat(historial1.getFirst().getFecha())
                .isNotNull();

        assertThat(historial2)
                .hasSize(1);

        assertThat(historial2.getFirst().getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.CLIENTE_RECHAZA_RENOVACION
                );

        assertThat(historial2.getFirst().getDetalle())
                .isEqualTo(
                        "El cliente ha rechazado la renovación del dominio"
                );

        assertThat(historial2.getFirst().getFecha())
                .isNotNull();
    }

    @Test
    void procesarConfirmacion_guardaFechaInteraccionDelCliente() {

        // ARRANGE

        Cliente cliente = crearCliente();

        Dominio dominio = crearDominio(
                cliente,
                "ana.com"
        );

        TokenCliente token = crearToken(cliente);

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

        assertThat(tokenDominios.getFirst()
                .getFechaInteraccionCliente())
                .isNotNull();
    }

    @Test
    void procesarConfirmacion_tokenYaUsado_puedeProcesarLaRespuesta() {

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

        TokenDominio tokenDominio =
                tokenDominioRepository
                        .findByTokenCliente(tokenActualizado)
                        .getFirst();

        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(
                        EstadoAvisoRenovacion.CONFIRMADO
                );

        assertThat(tokenActualizado.isUsado())
                .isTrue();
    }

    // ============================================================
    // validarToken()
    // ============================================================

    @Test
    void validarToken_tokenValido_devuelveValido() {

        crearToken(
                "token-valido",
                LocalDateTime.now().plusDays(5),
                false
        );

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-valido");

        assertThat(resultado.resultado())
                .isEqualTo(
                        ResultadoValidacion.VALIDO
                );

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().getToken())
                .isEqualTo("token-valido");

        assertThat(resultado.token().isUsado())
                .isFalse();
    }

    @Test
    void validarToken_tokenUsado_devuelveUsado() {

        crearToken(
                "token-usado",
                LocalDateTime.now().plusDays(5),
                true
        );

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-usado");

        assertThat(resultado.resultado())
                .isEqualTo(
                        ResultadoValidacion.USADO
                );

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().isUsado())
                .isTrue();
    }

    @Test
    void validarToken_tokenExpirado_devuelveExpirado() {

        crearToken(
                "token-expirado",
                LocalDateTime.now().minusDays(1),
                false
        );

        ResultadoValidacionRec resultado =
                tokenService.validarToken("token-expirado");

        assertThat(resultado.resultado())
                .isEqualTo(
                        ResultadoValidacion.EXPIRADO
                );

        assertThat(resultado.token())
                .isNotNull();

        assertThat(resultado.token().isUsado())
                .isFalse();
    }

    @Test
    void validarToken_tokenNoEncontrado_devuelveNoEncontrado() {

        ResultadoValidacionRec resultado =
                tokenService.validarToken(
                        "token-inexistente"
                );

        assertThat(resultado.resultado())
                .isEqualTo(
                        ResultadoValidacion.NO_ENCONTRADO
                );

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