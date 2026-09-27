package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GestionDominioServiceIntegrationTest {

    @Autowired
    private GestionDominioService gestionDominioService;

    @Autowired
    private FacturacionService facturacionService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @Autowired
    private HistorialDominioRepository historialDominioRepository;

    @BeforeEach
    void limpiarDatos() {

        historialDominioRepository.deleteAll();
        facturacionRepository.deleteAll();
        tokenDominioRepository.deleteAll();
        tokenClienteRepository.deleteAll();
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // =========================================================
    // FLUJO DE RENOVACIÓN
    // =========================================================

    @Test
    void marcarComoRenovado_flujoCompleto_actualizaDominioYGuardaHistorial() {

        // -----------------------------------------------------
        // ARRANGE
        // -----------------------------------------------------

        Cliente cliente = crearCliente(
                "Cliente Renovacion",
                "renovacion@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "integracion-renovacion.es"
        );

        TokenCliente tokenCliente = crearTokenCliente(cliente);

        TokenDominio tokenDominio = new TokenDominio();
        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.CONFIRMADO
        );
        tokenDominioRepository.saveAndFlush(tokenDominio);

        // -----------------------------------------------------
        // ASSERT PREVIO
        // -----------------------------------------------------

        assertThat(dominioRepository
                .findById(dominio.getId()))
                .isPresent()
                .get()
                .extracting(Dominio::getEstadoRenovacion)
                .isEqualTo(EstadoRenovacion.PENDIENTE_RENOVACION);

        assertThat(
                gestionDominioService
                        .puedeMarcarComoRenovado(dominio.getId())
        ).isTrue();

        // -----------------------------------------------------
        // ACT
        // -----------------------------------------------------

        gestionDominioService.marcarComoRenovado(
                dominio.getId()
        );

        // -----------------------------------------------------
        // ASSERT - DOMINIO
        // -----------------------------------------------------

        Dominio dominioActualizado =
                dominioRepository
                        .findById(dominio.getId())
                        .orElseThrow();

        assertThat(dominioActualizado.getEstadoRenovacion())
                .isEqualTo(EstadoRenovacion.RENOVADO);

        // -----------------------------------------------------
        // ASSERT - HISTORIAL
        // -----------------------------------------------------

        List<HistorialDominio> historial =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioActualizado
                        );

        assertThat(historial)
                .hasSize(1);

        HistorialDominio evento =
                historial.getFirst();

        assertThat(evento.getDominio().getId())
                .isEqualTo(dominio.getId());

        assertThat(evento.getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.RENOVACION_REALIZADA
                );

        assertThat(evento.getDetalle())
                .isEqualTo(
                        "Renovación realizada en el registrador"
                );

        assertThat(evento.getFecha())
                .isNotNull();

        // -----------------------------------------------------
        // ASSERT - YA NO SE PUEDE RENOVAR OTRA VEZ
        // -----------------------------------------------------

        assertThat(
                gestionDominioService
                        .puedeMarcarComoRenovado(dominio.getId())
        ).isFalse();
    }

    // =========================================================
    // FLUJO DE FACTURACIÓN
    // =========================================================

    @Test
    void marcarComoFacturado_flujoCompleto_actualizaFacturacionYGuardaHistorial() {

        // -----------------------------------------------------
        // ARRANGE
        // -----------------------------------------------------

        Cliente cliente = crearCliente(
                "Cliente Facturacion",
                "facturacion@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "integracion-facturacion.es"
        );

        dominio.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );

        dominio = dominioRepository.saveAndFlush(dominio);

        Facturacion facturacion = new Facturacion();

        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        facturacionRepository.saveAndFlush(
                facturacion
        );

        // -----------------------------------------------------
        // ASSERT PREVIO
        // -----------------------------------------------------

        Facturacion facturacionInicial =
                facturacionRepository
                        .findByDominio(dominio)
                        .orElseThrow();

        assertThat(facturacionInicial
                .getEstadoFacturacion())
                .isEqualTo(
                        EstadoFacturacion.PENDIENTE_FACTURAR
                );

        assertThat(facturacionInicial
                .getFechaUltimaFactura())
                .isNull();

        // -----------------------------------------------------
        // ACT
        // -----------------------------------------------------

        facturacionService.marcarComoFacturado(
                dominio.getId()
        );

        // -----------------------------------------------------
        // ASSERT - FACTURACIÓN
        // -----------------------------------------------------

        Facturacion facturacionActualizada =
                facturacionRepository
                        .findByDominio(dominio)
                        .orElseThrow();

        assertThat(
                facturacionActualizada.getEstadoFacturacion()
        ).isEqualTo(
                EstadoFacturacion.FACTURADO
        );

        assertThat(
                facturacionActualizada.getFechaUltimaFactura()
        ).isEqualTo(LocalDate.now());

        // -----------------------------------------------------
        // ASSERT - HISTORIAL
        // -----------------------------------------------------

        List<HistorialDominio> historial =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominio
                        );

        assertThat(historial)
                .hasSize(1);

        HistorialDominio evento =
                historial.getFirst();

        assertThat(evento.getDominio().getId())
                .isEqualTo(dominio.getId());

        assertThat(evento.getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.FACTURACION_REALIZADA
                );

        assertThat(evento.getDetalle())
                .isEqualTo("Facturación realizada");

        assertThat(evento.getFecha())
                .isNotNull();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Cliente crearCliente(
            String nombre,
            String email
    ) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return clienteRepository.saveAndFlush(
                cliente
        );
    }

    private Dominio crearDominio(
            Cliente cliente,
            String nombre
    ) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio(nombre);
        dominio.setEstado(Estado.ACTIVO);
        dominio.setEstadoRenovacion(
                EstadoRenovacion.PENDIENTE_RENOVACION
        );
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );
        dominio.setRegistrador(
                Registrador.DOMITECA
        );

        return dominioRepository.saveAndFlush(
                dominio
        );
    }

    private TokenCliente crearTokenCliente(
            Cliente cliente
    ) {

        TokenCliente tokenCliente =
                new TokenCliente();

        tokenCliente.setCliente(cliente);
        tokenCliente.setToken(
                "token-integracion-" + System.nanoTime()
        );
        tokenCliente.setFechaCreacion(
                LocalDateTime.now()
        );
        tokenCliente.setFechaExpiracion(
                LocalDateTime.now().plusDays(7)
        );
        tokenCliente.setUsado(false);

        return tokenClienteRepository.saveAndFlush(
                tokenCliente
        );
    }
}
