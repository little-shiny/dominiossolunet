package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ClienteResumen;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private Model model;

    @InjectMocks
    private ClienteController clienteController;

    private Cliente cliente;
    private Dominio dominio1;
    private Dominio dominio2;

    @BeforeEach
    void setUp() {

        cliente = new Cliente();

        ReflectionTestUtils.setField(cliente, "id", 1);

        cliente.setNombre("Cliente Uno");
        cliente.setEmail("cliente1@test.com");

        dominio1 = new Dominio();
        ReflectionTestUtils.setField(dominio1, "id", 1);
        dominio1.setNombreDominio("dominio1.es");
        dominio1.setCliente(cliente);

        dominio2 = new Dominio();
        ReflectionTestUtils.setField(dominio2, "id", 2);
        dominio2.setNombreDominio("dominio2.es");
        dominio2.setCliente(cliente);
    }

    @Test
    void listarClientes_devuelveVistaClientes() {

        ClienteResumen resumen = new ClienteResumen(
                cliente,
                2,
                1,
                1,
                0,
                1
        );

        when(clienteService.obtenerResumenClientes())
                .thenReturn(List.of(resumen));

        String vista = clienteController.listarClientes(model);

        assertThat(vista)
                .isEqualTo("gestion/clientes");

        verify(clienteService)
                .obtenerResumenClientes();

        verify(model)
                .addAttribute(
                        "clientes",
                        List.of(resumen)
                );
    }

    @Test
    void listarClientes_sinClientes_devuelveListaVacia() {

        when(clienteService.obtenerResumenClientes())
                .thenReturn(List.of());

        String vista = clienteController.listarClientes(model);

        assertThat(vista)
                .isEqualTo("gestion/clientes");

        verify(clienteService)
                .obtenerResumenClientes();

        verify(model)
                .addAttribute(
                        "clientes",
                        List.of()
                );
    }

    @Test
    void detalleCliente_devuelveVistaDetalleCliente() {

        List<Dominio> dominios =
                List.of(dominio1, dominio2);

        when(clienteService.obtenerCliente(1))
                .thenReturn(cliente);

        when(clienteService.obtenerDominiosCliente(1))
                .thenReturn(dominios);

        when(clienteService.contarDominios(dominios))
                .thenReturn(2L);

        when(clienteService.contarPendientesRenovacion(dominios))
                .thenReturn(1L);

        when(clienteService.contarRenovados(dominios))
                .thenReturn(1L);

        when(clienteService.contarRechazados(dominios))
                .thenReturn(0L);

        when(clienteService.contarPendientesFacturacion(dominios))
                .thenReturn(1L);

        String vista =
                clienteController.detalleCliente(1, model);

        assertThat(vista)
                .isEqualTo("gestion/cliente-detalle");

        verify(clienteService)
                .obtenerCliente(1);

        verify(clienteService)
                .obtenerDominiosCliente(1);

        verify(clienteService)
                .contarDominios(dominios);

        verify(clienteService)
                .contarPendientesRenovacion(dominios);

        verify(clienteService)
                .contarRenovados(dominios);

        verify(clienteService)
                .contarRechazados(dominios);

        verify(clienteService)
                .contarPendientesFacturacion(dominios);

        verify(model)
                .addAttribute("cliente", cliente);

        verify(model)
                .addAttribute("dominios", dominios);

        verify(model)
                .addAttribute("totalDominios", 2L);

        verify(model)
                .addAttribute("pendientesRenovacion", 1L);

        verify(model)
                .addAttribute("renovados", 1L);

        verify(model)
                .addAttribute("rechazados", 0L);

        verify(model)
                .addAttribute("pendientesFacturacion", 1L);
    }

    @Test
    void detalleCliente_clienteSinDominios_muestraValoresCero() {

        List<Dominio> dominios = List.of();

        when(clienteService.obtenerCliente(1))
                .thenReturn(cliente);

        when(clienteService.obtenerDominiosCliente(1))
                .thenReturn(dominios);

        when(clienteService.contarDominios(dominios))
                .thenReturn(0L);

        when(clienteService.contarPendientesRenovacion(dominios))
                .thenReturn(0L);

        when(clienteService.contarRenovados(dominios))
                .thenReturn(0L);

        when(clienteService.contarRechazados(dominios))
                .thenReturn(0L);

        when(clienteService.contarPendientesFacturacion(dominios))
                .thenReturn(0L);

        String vista =
                clienteController.detalleCliente(1, model);

        assertThat(vista)
                .isEqualTo("gestion/cliente-detalle");

        verify(model)
                .addAttribute("cliente", cliente);

        verify(model)
                .addAttribute("dominios", dominios);

        verify(model)
                .addAttribute("totalDominios", 0L);

        verify(model)
                .addAttribute("pendientesRenovacion", 0L);

        verify(model)
                .addAttribute("renovados", 0L);

        verify(model)
                .addAttribute("rechazados", 0L);

        verify(model)
                .addAttribute("pendientesFacturacion", 0L);
    }
}
