package com.dominiossolunet.controller;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

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
    private HistorialDominioRepository historialDominioRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {

        // Primero las entidades que dependen de Dominio
        historialDominioRepository.deleteAll();
        tokenDominioRepository.deleteAll();
        facturacionRepository.deleteAll();

        // Después las entidades que dependen de Cliente
        tokenClienteRepository.deleteAll();

        // Finalmente dominios y clientes
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void listarClientes_muestraClientesYEstadisticas() throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Integracion",
                "integracion@cliente.com"
        );

        crearDominio(
                cliente,
                "dominio-pendiente.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA
        );

        crearDominio(
                cliente,
                "dominio-renovado.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI
        );

        mockMvc.perform(get("/gestion/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/clientes"))
                .andExpect(content().string(containsString("Cliente Integracion")))
                .andExpect(content().string(containsString("integracion@cliente.com")))
                .andExpect(content().string(containsString("1 clientes encontrados")))
                .andExpect(content().string(containsString(">2</span>")))
                .andExpect(content().string(containsString(">1</span>")));
    }

    @Test
    void listarClientes_sinClientes_muestraEstadoVacio() throws Exception {

        mockMvc.perform(get("/gestion/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/clientes"))
                .andExpect(content().string(
                        containsString("No hay clientes")
                ));
    }

    @Test
    void detalleCliente_muestraInformacionDelCliente() throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Detalle",
                "detalle@cliente.com"
        );

        crearDominio(
                cliente,
                "detalle.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA
        );

        mockMvc.perform(get("/gestion/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(content().string(
                        containsString("Cliente Detalle")
                ))
                .andExpect(content().string(
                        containsString("detalle@cliente.com")
                ))
                .andExpect(content().string(
                        containsString("detalle.es")
                ));
    }

    @Test
    void detalleCliente_muestraEstadisticasCorrectamente() throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Estadisticas",
                "estadisticas@cliente.com"
        );

        crearDominio(
                cliente,
                "pendiente.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA
        );

        crearDominio(
                cliente,
                "renovado.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI
        );

        crearDominio(
                cliente,
                "rechazado.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.RECHAZADO,
                Registrador.NOMINALIA
        );

        mockMvc.perform(get("/gestion/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(content().string(
                        containsString("3")
                ))
                .andExpect(content().string(
                        containsString("pendiente.es")
                ))
                .andExpect(content().string(
                        containsString("renovado.es")
                ))
                .andExpect(content().string(
                        containsString("rechazado.es")
                ));
    }

    @Test
    void detalleCliente_sinDominios_muestraClienteSinDominios() throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Sin Dominios",
                "sin@cliente.com"
        );

        mockMvc.perform(get("/gestion/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(content().string(
                        containsString("Cliente Sin Dominios")
                ))
                .andExpect(content().string(
                        containsString("sin@cliente.com")
                ));
    }

    @Test
    void detalleCliente_noMuestraDominiosDeOtroCliente() throws Exception {

        Cliente clientePrincipal = crearCliente(
                "Cliente Principal",
                "principal@cliente.com"
        );

        Cliente otroCliente = crearCliente(
                "Otro Cliente",
                "otro@cliente.com"
        );

        crearDominio(
                clientePrincipal,
                "principal.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA
        );

        crearDominio(
                otroCliente,
                "otro.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI
        );

        mockMvc.perform(
                        get("/gestion/clientes/{id}", clientePrincipal.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/cliente-detalle"))
                .andExpect(content().string(
                        containsString("principal.es")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                containsString("otro.es")
                        )
                ));
    }

    @Test
    void detalleCliente_clienteNoExistente_lanzaServletExceptionConIllegalArgumentExceptionComoCausa()
            throws Exception {

        try {

            mockMvc.perform(
                    get("/gestion/clientes/{id}", 999999)
            );

        } catch (ServletException exception) {

            Throwable causa = exception;

            boolean encontrada = false;

            while (causa != null) {

                if (causa instanceof IllegalArgumentException) {
                    encontrada = true;

                    assertTrue(
                            causa.getMessage().contains(
                                    "Cliente no encontrado"
                            )
                    );

                    break;
                }

                causa = causa.getCause();
            }

            assertTrue(
                    encontrada,
                    "Se esperaba una IllegalArgumentException como causa"
            );
        }
    }

    private Cliente crearCliente(String nombre, String email) {

        Cliente cliente = new Cliente();

        cliente.setNombre(nombre);
        cliente.setEmail(email);

        return clienteRepository.save(cliente);
    }

    private Dominio crearDominio(
            Cliente cliente,
            String nombre,
            Estado estado,
            EstadoRenovacion estadoRenovacion,
            Registrador registrador) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio(nombre);
        dominio.setEstado(estado);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(registrador);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(30)
        );

        return dominioRepository.save(dominio);
    }
}
