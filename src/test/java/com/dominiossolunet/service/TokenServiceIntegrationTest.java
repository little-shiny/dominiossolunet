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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test de integración para TokenService centrado en el comportamiento de persistencia
 * (managed vs detached) al modificar entidades a través de varias "transacciones lógicas",
 * simulando lo que ocurriría entre una petición GET (validarToken) y una petición POST
 * (marcaEstadoRenovacionPorListaIdDominio) reales.
 *
 * Se usa @DataJpaTest porque nos da una base H2 real y TestEntityManager, pero como
 * TokenService es un @Service (no un Repository ni una @Entity), @DataJpaTest no lo
 * registra por defecto — hay que importarlo explícitamente con @Import.
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

        // 1. Preparar datos: Cliente -> Dominio -> TokenCliente -> TokenDominio
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");
        entityManager.persistAndFlush(cliente);

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setCliente(cliente);
        dominio.setRegistrador(Registrador.DOMITECA);
        entityManager.persistAndFlush(dominio);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("abc123");
        tokenCliente.setUsado(false);
        tokenCliente.setFechaCreacion(LocalDateTime.now());
        tokenCliente.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenCliente.setCliente(cliente);
        entityManager.persistAndFlush(tokenCliente);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.PENDIENTE);
        entityManager.persistAndFlush(tokenDominio);

        // 2. Simular la petición GET: se recupera el token tal como haría validarToken()
        TokenCliente tokenRecuperado = tokenClienteRepository.findByToken("abc123").orElseThrow();

        // 3. Punto clave: se vacía el contexto de persistencia.
        //    Esto simula que, entre el GET y el POST, no hay ningún EntityManager compartido
        //    (como pasaría de verdad entre dos peticiones HTTP distintas).
        //    A partir de aquí, tokenRecuperado queda DETACHED.
        entityManager.clear();

        // 4. Se construye el record tal como llegaría desde el controller en el POST
        ResultadoValidacionRec resultadoValidacion =
                new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenRecuperado);

        // 5. Se llama al método bajo prueba con el objeto detached
        tokenService.marcaEstadoRenovacionPorListaIdDominio(
                List.of(dominio.getId()), resultadoValidacion);

        // 6. Se limpia otra vez para forzar una lectura fresca desde la base de datos,
        //    y no desde la caché del contexto de persistencia.
        entityManager.flush();
        entityManager.clear();

        // 7. Verificación
        TokenCliente comprobacion = tokenClienteRepository.findByToken("abc123").orElseThrow();
        TokenDominio tokenDominioComprobacion = tokenDominioRepository.findByTokenCliente(comprobacion).getFirst();

        assertThat(tokenDominioComprobacion.getEstadoAvisoRenovacion()).isEqualTo(EstadoAvisoRenovacion.CONFIRMADO);
        assertThat(comprobacion.isUsado()).isTrue();
    }

    //TODO completar el test al actualizar POST del controller
}