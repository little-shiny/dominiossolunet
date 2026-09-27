package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClienteServiceIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @Autowired
    private HistorialDominioRepository historialDominioRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @BeforeEach
    @AfterEach
    void limpiarDatos() {

        historialDominioRepository.deleteAll();
        tokenDominioRepository.deleteAll();
        facturacionRepository.deleteAll();
        tokenClienteRepository.deleteAll();
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void obtenerDominiosCliente_devuelveSoloLosDominiosDelCliente() {

        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Cliente Uno");
        cliente1.setEmail("cliente1@test.com");
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Cliente Dos");
        cliente2.setEmail("cliente2@test.com");
        cliente2 = clienteRepository.save(cliente2);

        Dominio dominio1 = new Dominio();
        dominio1.setNombreDominio("dominio1.es");
        dominio1.setCliente(cliente1);
        dominio1.setEstado(Estado.ACTIVO);
        dominio1.setEstadoRenovacion(
                EstadoRenovacion.PENDIENTE_RENOVACION
        );
        dominio1.setRegistrador(Registrador.DOMITECA);
        dominio1.setFechaExpiracion(
                LocalDate.of(2026, 12, 31)
        );
        dominio1 = dominioRepository.save(dominio1);

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("dominio2.es");
        dominio2.setCliente(cliente1);
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );
        dominio2.setRegistrador(Registrador.GANDI);
        dominio2.setFechaExpiracion(
                LocalDate.of(2027, 1, 31)
        );
        dominio2 = dominioRepository.save(dominio2);

        Dominio dominio3 = new Dominio();
        dominio3.setNombreDominio("dominio3.es");
        dominio3.setCliente(cliente2);
        dominio3.setEstado(Estado.ACTIVO);
        dominio3.setEstadoRenovacion(
                EstadoRenovacion.RECHAZADO
        );
        dominio3.setRegistrador(Registrador.NOMINALIA);
        dominio3.setFechaExpiracion(
                LocalDate.of(2027, 2, 28)
        );
        dominio3 = dominioRepository.save(dominio3);

        List<Dominio> resultado =
                clienteService.obtenerDominiosCliente(cliente1.getId());

        assertThat(resultado)
                .hasSize(2);

        assertThat(resultado)
                .extracting(Dominio::getId)
                .containsExactlyInAnyOrder(
                        dominio1.getId(),
                        dominio2.getId()
                );

        assertThat(resultado)
                .extracting(Dominio::getNombreDominio)
                .containsExactlyInAnyOrder(
                        "dominio1.es",
                        "dominio2.es"
                );

        assertThat(resultado)
                .extracting(dominio -> dominio.getCliente().getId())
                .containsOnly(cliente1.getId());

        assertThat(resultado)
                .extracting(Dominio::getId)
                .doesNotContain(dominio3.getId());
    }

    @Test
    void obtenerDominiosCliente_clienteSinDominios_devuelveListaVacia() {

        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Cliente Uno");
        cliente1.setEmail("cliente1@test.com");
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Cliente Dos");
        cliente2.setEmail("cliente2@test.com");
        cliente2 = clienteRepository.save(cliente2);

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("dominio2.es");
        dominio.setCliente(cliente2);
        dominio.setEstado(Estado.ACTIVO);
        dominio.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );
        dominio.setRegistrador(Registrador.GANDI);
        dominio.setFechaExpiracion(
                LocalDate.of(2027, 1, 31)
        );

        dominioRepository.save(dominio);

        List<Dominio> resultado =
                clienteService.obtenerDominiosCliente(cliente1.getId());

        assertThat(resultado)
                .isEmpty();
    }

    @Test
    void obtenerCliente_clienteExiste_devuelveCliente() {

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Uno");
        cliente.setEmail("cliente1@test.com");

        cliente = clienteRepository.save(cliente);

        Cliente resultado =
                clienteService.obtenerCliente(cliente.getId());

        assertThat(resultado.getId())
                .isEqualTo(cliente.getId());

        assertThat(resultado.getNombre())
                .isEqualTo("Cliente Uno");

        assertThat(resultado.getEmail())
                .isEqualTo("cliente1@test.com");
    }

    @Test
    void obtenerCliente_clienteNoExiste_lanzaExcepcion() {

        assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.obtenerCliente(999999)
                ).getMessage()
        ).isEqualTo("Cliente no encontrado: 999999");
    }

    @Test
    void contarDominios_devuelveNumeroCorrecto() {

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Uno");
        cliente.setEmail("cliente1@test.com");
        cliente = clienteRepository.save(cliente);

        Dominio dominio1 = crearDominio(
                "dominio1.es",
                cliente,
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        Dominio dominio2 = crearDominio(
                "dominio2.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio3 = crearDominio(
                "dominio3.es",
                cliente,
                EstadoRenovacion.RECHAZADO
        );

        List<Dominio> dominios = List.of(
                dominio1,
                dominio2,
                dominio3
        );

        assertThat(
                clienteService.contarDominios(dominios)
        ).isEqualTo(3);
    }

    @Test
    void contarPendientesRenovacion_devuelveNumeroCorrecto() {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                "pendiente.es",
                cliente,
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        Dominio dominio2 = crearDominio(
                "renovado.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio3 = crearDominio(
                "rechazado.es",
                cliente,
                EstadoRenovacion.RECHAZADO
        );

        assertThat(
                clienteService.contarPendientesRenovacion(
                        List.of(dominio1, dominio2, dominio3)
                )
        ).isEqualTo(1);
    }

    @Test
    void contarRenovados_devuelveNumeroCorrecto() {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                "pendiente.es",
                cliente,
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        Dominio dominio2 = crearDominio(
                "renovado.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio3 = crearDominio(
                "rechazado.es",
                cliente,
                EstadoRenovacion.RECHAZADO
        );

        assertThat(
                clienteService.contarRenovados(
                        List.of(dominio1, dominio2, dominio3)
                )
        ).isEqualTo(1);
    }

    @Test
    void contarRechazados_devuelveNumeroCorrecto() {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                "pendiente.es",
                cliente,
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        Dominio dominio2 = crearDominio(
                "renovado.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio3 = crearDominio(
                "rechazado.es",
                cliente,
                EstadoRenovacion.RECHAZADO
        );

        assertThat(
                clienteService.contarRechazados(
                        List.of(dominio1, dominio2, dominio3)
                )
        ).isEqualTo(1);
    }

    @Test
    void contarPendientesFacturacion_devuelveNumeroCorrecto() {

        Cliente cliente = crearCliente();

        Dominio dominio1 = crearDominio(
                "pendiente-facturar.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio2 = crearDominio(
                "facturado.es",
                cliente,
                EstadoRenovacion.RENOVADO
        );

        dominio1 = dominioRepository.save(dominio1);
        dominio2 = dominioRepository.save(dominio2);

        Facturacion facturacionPendiente = new Facturacion();
        facturacionPendiente.setDominio(dominio1);
        facturacionPendiente.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );
        facturacionRepository.save(facturacionPendiente);

        Facturacion facturacionRealizada = new Facturacion();
        facturacionRealizada.setDominio(dominio2);
        facturacionRealizada.setEstadoFacturacion(
                EstadoFacturacion.FACTURADO
        );
        facturacionRepository.save(facturacionRealizada);

        assertThat(
                clienteService.contarPendientesFacturacion(
                        List.of(dominio1, dominio2)
                )
        ).isEqualTo(1);
    }

    @Test
    void obtenerResumenClientes_calculaCorrectamenteLosDatos() {

        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Cliente Uno");
        cliente1.setEmail("cliente1@test.com");
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Cliente Dos");
        cliente2.setEmail("cliente2@test.com");
        cliente2 = clienteRepository.save(cliente2);

        Dominio dominio1 = crearDominio(
                "pendiente.es",
                cliente1,
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        Dominio dominio2 = crearDominio(
                "renovado.es",
                cliente1,
                EstadoRenovacion.RENOVADO
        );

        Dominio dominio3 = crearDominio(
                "rechazado.es",
                cliente2,
                EstadoRenovacion.RECHAZADO
        );

        dominio1 = dominioRepository.save(dominio1);
        dominio2 = dominioRepository.save(dominio2);
        dominio3 = dominioRepository.save(dominio3);

        Facturacion facturacion = new Facturacion();
        facturacion.setDominio(dominio1);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );
        facturacionRepository.save(facturacion);

        List<com.dominiossolunet.dto.ClienteResumen> resultado =
                clienteService.obtenerResumenClientes();

        assertThat(resultado)
                .hasSize(2);

        Cliente finalCliente = cliente1;
        com.dominiossolunet.dto.ClienteResumen resumenCliente1 =
                resultado.stream()
                        .filter(r ->
                                r.getCliente().getId()
                                        == finalCliente.getId())
                        .findFirst()
                        .orElseThrow();

        assertThat(resumenCliente1.getTotalDominios())
                .isEqualTo(2);

        assertThat(resumenCliente1.getPendientesRenovacion())
                .isEqualTo(1);

        assertThat(resumenCliente1.getRenovados())
                .isEqualTo(1);

        assertThat(resumenCliente1.getRechazados())
                .isEqualTo(0);

        assertThat(resumenCliente1.getPendientesFacturacion())
                .isEqualTo(1);

        Cliente finalCliente1 = cliente2;
        com.dominiossolunet.dto.ClienteResumen resumenCliente2 =
                resultado.stream()
                        .filter(r ->
                                r.getCliente().getId()
                                        == finalCliente1.getId())
                        .findFirst()
                        .orElseThrow();

        assertThat(resumenCliente2.getTotalDominios())
                .isEqualTo(1);

        assertThat(resumenCliente2.getPendientesRenovacion())
                .isEqualTo(0);

        assertThat(resumenCliente2.getRenovados())
                .isEqualTo(0);

        assertThat(resumenCliente2.getRechazados())
                .isEqualTo(1);

        assertThat(resumenCliente2.getPendientesFacturacion())
                .isEqualTo(0);
    }

    private Cliente crearCliente() {

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Test");
        cliente.setEmail(
                "cliente-" + System.nanoTime() + "@test.com"
        );

        return clienteRepository.save(cliente);
    }

    private Dominio crearDominio(
            String nombre,
            Cliente cliente,
            EstadoRenovacion estadoRenovacion) {

        Dominio dominio = new Dominio();

        dominio.setNombreDominio(nombre);
        dominio.setCliente(cliente);
        dominio.setEstado(Estado.ACTIVO);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(Registrador.DOMITECA);
        dominio.setFechaExpiracion(
                LocalDate.of(2027, 1, 1)
        );

        return dominio;
    }
}

