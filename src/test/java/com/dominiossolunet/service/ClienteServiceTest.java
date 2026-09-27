package com.dominiossolunet.service;

import com.dominiossolunet.dto.ClienteResumen;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private FacturacionRepository facturacionRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente1;
    private Cliente cliente2;

    private Dominio dominio1;
    private Dominio dominio2;
    private Dominio dominio3;

    private Facturacion facturacion1;
    private Facturacion facturacion2;

    @BeforeEach
    void setUp() {

        cliente1 = new Cliente();
        ReflectionTestUtils.setField(cliente1, "id", 1);
        cliente1.setNombre("Cliente Uno");
        cliente1.setEmail("cliente1@test.com");

        cliente2 = new Cliente();
        ReflectionTestUtils.setField(cliente2, "id", 2);
        cliente2.setNombre("Cliente Dos");
        cliente2.setEmail("cliente2@test.com");

        dominio1 = new Dominio();
        ReflectionTestUtils.setField(dominio1, "id", 1);
        dominio1.setNombreDominio("dominio1.es");
        dominio1.setCliente(cliente1);
        dominio1.setEstadoRenovacion(
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        dominio2 = new Dominio();
        ReflectionTestUtils.setField(dominio2, "id", 2);
        dominio2.setNombreDominio("dominio2.es");
        dominio2.setCliente(cliente1);
        dominio2.setEstadoRenovacion(
                EstadoRenovacion.RENOVADO
        );

        dominio3 = new Dominio();
        ReflectionTestUtils.setField(dominio3, "id", 3);
        dominio3.setNombreDominio("dominio3.es");
        dominio3.setCliente(cliente2);
        dominio3.setEstadoRenovacion(
                EstadoRenovacion.RECHAZADO
        );

        facturacion1 = new Facturacion();
        ReflectionTestUtils.setField(facturacion1, "id", 1);
        facturacion1.setDominio(dominio1);
        facturacion1.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        facturacion2 = new Facturacion();
        ReflectionTestUtils.setField(facturacion2, "id", 2);
        facturacion2.setDominio(dominio2);
        facturacion2.setEstadoFacturacion(
                EstadoFacturacion.FACTURADO
        );
    }

    @Test
    void obtenerResumenClientes_calculaCorrectamenteLosDatos() {

        when(clienteRepository.findAll())
                .thenReturn(List.of(cliente1, cliente2));

        when(dominioRepository.findAll())
                .thenReturn(List.of(dominio1, dominio2, dominio3));

        when(facturacionRepository.findAll())
                .thenReturn(List.of(facturacion1, facturacion2));

        List<ClienteResumen> resultado =
                clienteService.obtenerResumenClientes();

        assertThat(resultado).hasSize(2);

        ClienteResumen resumenCliente1 = resultado.get(0);

        assertThat(resumenCliente1.getCliente())
                .isEqualTo(cliente1);

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

        ClienteResumen resumenCliente2 = resultado.get(1);

        assertThat(resumenCliente2.getCliente())
                .isEqualTo(cliente2);

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

        verify(clienteRepository).findAll();
        verify(dominioRepository).findAll();
        verify(facturacionRepository).findAll();
    }

    @Test
    void obtenerResumenClientes_sinClientes_devuelveListaVacia() {

        when(clienteRepository.findAll())
                .thenReturn(List.of());

        when(dominioRepository.findAll())
                .thenReturn(List.of());

        when(facturacionRepository.findAll())
                .thenReturn(List.of());

        List<ClienteResumen> resultado =
                clienteService.obtenerResumenClientes();

        assertThat(resultado).isEmpty();

        verify(clienteRepository).findAll();
        verify(dominioRepository).findAll();
        verify(facturacionRepository).findAll();
    }

    @Test
    void obtenerCliente_clienteExiste_devuelveCliente() {

        when(clienteRepository.findById(1))
                .thenReturn(Optional.of(cliente1));

        Cliente resultado =
                clienteService.obtenerCliente(1);

        assertThat(resultado).isSameAs(cliente1);

        verify(clienteRepository).findById(1);
    }

    @Test
    void obtenerCliente_clienteNoExiste_lanzaExcepcion() {

        when(clienteRepository.findById(99))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.obtenerCliente(99)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Cliente no encontrado: 99");

        verify(clienteRepository).findById(99);
    }

    @Test
    void obtenerDominiosCliente_devuelveSoloLosDominiosDelCliente() {

        when(dominioRepository.findAll())
                .thenReturn(List.of(
                        dominio1,
                        dominio2,
                        dominio3
                ));

        List<Dominio> resultado =
                clienteService.obtenerDominiosCliente(1);

        assertThat(resultado)
                .containsExactly(dominio1, dominio2);

        assertThat(resultado)
                .doesNotContain(dominio3);

        verify(dominioRepository).findAll();
    }

    @Test
    void obtenerDominiosCliente_clienteSinDominios_devuelveListaVacia() {

        Dominio dominioDeOtroCliente = new Dominio();
        ReflectionTestUtils.setField(dominioDeOtroCliente, "id", 10);
        dominioDeOtroCliente.setCliente(cliente2);

        when(dominioRepository.findAll())
                .thenReturn(List.of(dominioDeOtroCliente));

        List<Dominio> resultado =
                clienteService.obtenerDominiosCliente(1);

        assertThat(resultado).isEmpty();

        verify(dominioRepository).findAll();
    }

    @Test
    void contarDominios_devuelveNumeroCorrecto() {

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarDominios(dominios);

        assertThat(resultado).isEqualTo(3);
    }

    @Test
    void contarPendientesRenovacion_devuelveNumeroCorrecto() {

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarPendientesRenovacion(dominios);

        assertThat(resultado).isEqualTo(1);
    }

    @Test
    void contarRenovados_devuelveNumeroCorrecto() {

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarRenovados(dominios);

        assertThat(resultado).isEqualTo(1);
    }

    @Test
    void contarRechazados_devuelveNumeroCorrecto() {

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarRechazados(dominios);

        assertThat(resultado).isEqualTo(1);
    }

    @Test
    void contarPendientesFacturacion_devuelveNumeroCorrecto() {

        when(facturacionRepository.findAll())
                .thenReturn(List.of(
                        facturacion1,
                        facturacion2
                ));

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarPendientesFacturacion(dominios);

        assertThat(resultado).isEqualTo(1);

        verify(facturacionRepository).findAll();
    }

    @Test
    void contarPendientesFacturacion_sinFacturaciones_devuelveCero() {

        when(facturacionRepository.findAll())
                .thenReturn(List.of());

        List<Dominio> dominios =
                List.of(dominio1, dominio2, dominio3);

        long resultado =
                clienteService.contarPendientesFacturacion(dominios);

        assertThat(resultado).isZero();

        verify(facturacionRepository).findAll();
    }
}
