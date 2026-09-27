package com.dominiossolunet.controller;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void listarClientes_devuelveVistaConClientes() throws Exception {

        Cliente cliente1 = crearCliente(
                "Cliente Uno",
                "uno@test.com"
        );

        Cliente cliente2 = crearCliente(
                "Cliente Dos",
                "dos@test.com"
        );

        crearDominio(
                "uno.es",
                cliente1,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(30)
        );

        crearDominio(
                "dos.es",
                cliente2,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.GANDI,
                LocalDate.now().plusDays(15)
        );

        mockMvc.perform(get("/gestion/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/clientes"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cliente Uno")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cliente Dos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("uno@test.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("dos@test.com")));
    }

    @Test
    void listarClientes_clienteConEstadisticasMuestraLosValoresCorrectos()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Estadisticas",
                "estadisticas@test.com"
        );

        crearDominio(
                "renovado.es",
                cliente,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(100)
        );

        crearDominio(
                "pendiente.es",
                cliente,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.GANDI,
                LocalDate.now().plusDays(10)
        );

        crearDominio(
                "rechazado.es",
                cliente,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.RECHAZADO,
                Registrador.NOMINALIA,
                LocalDate.now().plusDays(20)
        );

        Dominio dominioPendienteFacturacion = crearDominio(
                "facturar.es",
                cliente,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.OTRO,
                LocalDate.now().plusDays(200)
        );

        Facturacion facturacion = new Facturacion();
        facturacion.setDominio(dominioPendienteFacturacion);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        facturacionRepository.save(facturacion);

        mockMvc.perform(get("/gestion/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/clientes"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "Cliente Estadisticas"
                                )
                        )
                )
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        ">4</span>"
                                )
                        )
                );
    }

    @Test
    void listarClientes_sinClientesMuestraEstadoVacio()
            throws Exception {

        mockMvc.perform(get("/gestion/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/clientes"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "No hay clientes registrados."
                                )
                        )
                )
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "0 clientes encontrados"
                                )
                        )
                );
    }

    @Test
    void detalleCliente_devuelveVistaConDatosDelCliente()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Detalle",
                "detalle@test.com"
        );

        Dominio dominio1 = crearDominio(
                "detalle-uno.es",
                cliente,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.of(2026, 10, 10)
        );

        crearDominio(
                "detalle-dos.es",
                cliente,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.GANDI,
                LocalDate.of(2026, 11, 15)
        );

        mockMvc.perform(
                        get("/gestion/clientes/{id}", cliente.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(model().attributeExists("cliente"))
                .andExpect(model().attributeExists("dominios"))
                .andExpect(model().attributeExists("totalDominios"))
                .andExpect(model().attributeExists("pendientesRenovacion"))
                .andExpect(model().attributeExists("renovados"))
                .andExpect(model().attributeExists("rechazados"))
                .andExpect(model().attributeExists("pendientesFacturacion"))
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "Cliente Detalle"
                                )
                        )
                )
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "detalle@test.com"
                                )
                        )
                )
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "detalle-uno.es"
                                )
                        )
                )
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "detalle-dos.es"
                                )
                        )
                );

        // Evita que el compilador marque la variable como innecesaria
        // y deja explícito que el dominio pertenece al cliente creado.
        assertTrue(dominio1.getCliente().getId() == cliente.getId());
    }

    @Test
    void detalleCliente_muestraEstadisticasCorrectas()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Resumen",
                "resumen@test.com"
        );

        crearDominio(
                "renovado.es",
                cliente,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(100)
        );

        crearDominio(
                "pendiente.es",
                cliente,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.GANDI,
                LocalDate.now().plusDays(15)
        );

        crearDominio(
                "rechazado.es",
                cliente,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.RECHAZADO,
                Registrador.NOMINALIA,
                LocalDate.now().plusDays(20)
        );

        Dominio dominioFacturadoPendiente = crearDominio(
                "facturacion.es",
                cliente,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.OTRO,
                LocalDate.now().plusDays(200)
        );

        Facturacion facturacion = new Facturacion();
        facturacion.setDominio(dominioFacturadoPendiente);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        facturacionRepository.save(facturacion);

        mockMvc.perform(
                        get("/gestion/clientes/{id}", cliente.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(
                        model().attribute(
                                "totalDominios",
                                4L
                        )
                )
                .andExpect(
                        model().attribute(
                                "pendientesRenovacion",
                                1L
                        )
                )
                .andExpect(
                        model().attribute(
                                "renovados",
                                2L
                        )
                )
                .andExpect(
                        model().attribute(
                                "rechazados",
                                1L
                        )
                )
                .andExpect(
                        model().attribute(
                                "pendientesFacturacion",
                                1L
                        )
                );
    }

    @Test
    void detalleCliente_sinDominiosMuestraEstadoVacio()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Sin Dominios",
                "sindominios@test.com"
        );

        mockMvc.perform(
                        get("/gestion/clientes/{id}", cliente.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(model().attribute("totalDominios", 0L))
                .andExpect(model().attribute("pendientesRenovacion", 0L))
                .andExpect(model().attribute("renovados", 0L))
                .andExpect(model().attribute("rechazados", 0L))
                .andExpect(model().attribute("pendientesFacturacion", 0L))
                .andExpect(
                        content().string(
                                org.hamcrest.Matchers.containsString(
                                        "Este cliente no tiene dominios registrados."
                                )
                        )
                );
    }

    @Test
    void detalleCliente_clienteNoExiste_lanzaExcepcion()
            throws Exception {

        try {
            mockMvc.perform(
                    get("/gestion/clientes/{id}", 999999)
            );

            fail(
                    "Se esperaba IllegalArgumentException"
            );

        } catch (Exception exception) {

            Throwable actual = exception;

            while (actual != null) {

                if (actual instanceof IllegalArgumentException) {

                    assertTrue(
                            actual.getMessage()
                                    .contains("Cliente no encontrado")
                    );

                    return;
                }

                actual = actual.getCause();
            }

            throw exception;
        }
    }

    private Cliente crearCliente(
            String nombre,
            String email) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return clienteRepository.save(cliente);
    }

    private Dominio crearDominio(
            String nombreDominio,
            Cliente cliente,
            Estado estado,
            EstadoRenovacion estadoRenovacion,
            Registrador registrador,
            LocalDate fechaExpiracion) {

        Dominio dominio = new Dominio();

        dominio.setNombreDominio(nombreDominio);
        dominio.setCliente(cliente);
        dominio.setEstado(estado);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(registrador);
        dominio.setFechaExpiracion(fechaExpiracion);

        return dominioRepository.save(dominio);
    }
}
