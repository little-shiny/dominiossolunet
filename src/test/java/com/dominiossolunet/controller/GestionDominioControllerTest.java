package com.dominiossolunet.controller;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.service.FacturacionService;
import com.dominiossolunet.service.GestionDominioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionDominioControllerTest {

    @Mock
    private GestionDominioService gestionDominioService;

    @Mock
    private FacturacionService facturacionService;

    @Mock
    private Model model;

    private GestionDominioController controller;

    private Cliente clienteUno;
    private Cliente clienteDos;

    private Dominio dominioUno;
    private Dominio dominioDos;
    private Dominio dominioTres;
    private Dominio dominioCuatro;

    @BeforeEach
    void setUp() {

        controller = new GestionDominioController(
                gestionDominioService,
                facturacionService
        );

        clienteUno = new Cliente();
        clienteUno.setNombre("Cliente Uno");
        clienteUno.setEmail("uno@cliente.com");

        clienteDos = new Cliente();
        clienteDos.setNombre("Cliente Dos");
        clienteDos.setEmail("dos@cliente.com");

        // -----------------------------------------------------
        // Dominio 1
        // -----------------------------------------------------

        dominioUno = crearDominio(
                1,
                "uno.es",
                clienteUno,
                Estado.ACTIVO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        // -----------------------------------------------------
        // Dominio 2
        // -----------------------------------------------------

        dominioDos = crearDominio(
                2,
                "dos.com",
                clienteUno,
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(20)
        );

        // -----------------------------------------------------
        // Dominio 3
        // -----------------------------------------------------

        dominioTres = crearDominio(
                3,
                "tres.es",
                clienteDos,
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.RECHAZADO,
                Registrador.NOMINALIA,
                LocalDate.now().plusDays(40)
        );

        // -----------------------------------------------------
        // Dominio 4
        // -----------------------------------------------------

        dominioCuatro = crearDominio(
                4,
                "cuatro.com",
                clienteDos,
                Estado.EXPIRADO_SIN_RESPUESTA,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.OTRO,
                LocalDate.now().minusDays(5)
        );

        ReflectionTestUtils.setField(clienteUno, "id", 1);
        ReflectionTestUtils.setField(clienteDos, "id", 2);
    }

    // =========================================================
    // POST /dominios/{id}/renovar
    // =========================================================

    @Test
    void marcarComoRenovado_llamaAlServicioYRedirige() {

        String resultado =
                controller.marcarComoRenovado(1);

        assertThat(resultado)
                .isEqualTo(
                        "redirect:/gestion/dominios"
                );

        verify(gestionDominioService)
                .marcarComoRenovado(1);
    }

    // =========================================================
    // GET /dominios
    // =========================================================

    @Test
    void listarDominios_sinFiltros_devuelveTodosLosDominios() {
        List<Dominio> dominios = List.of(
                dominioUno,
                dominioDos,
                dominioTres,
                dominioCuatro
        );

        when(gestionDominioService.obtenerTodosLosDominios())
                .thenReturn(dominios);

        when(gestionDominioService.puedeMarcarComoRenovado(anyInt()))
                .thenReturn(false);

        String vista = controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                null,
                model
        );

        assertThat(vista).isEqualTo("gestion/dominios");

        List<Dominio> resultado = capturarUltimaListaDominios();

        assertThat(resultado).containsExactlyElementsOf(dominios);
    }

    @Test
    void listarDominios_filtroBusqueda_porNombreDominio() {

        prepararDominios();

        String resultado = controller.listarDominios(
                "uno.es",
                null,
                null,
                null,
                null,
                null,
                model
        );

        assertThat(resultado)
                .isEqualTo("gestion/dominios");

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioUno);
    }

    @Test
    void listarDominios_filtroBusqueda_porNombreCliente() {

        prepararDominios();

        String resultado = controller.listarDominios(
                "Cliente Uno",
                null,
                null,
                null,
                null,
                null,
                model
        );

        assertThat(resultado)
                .isEqualTo("gestion/dominios");

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(
                        dominioUno,
                        dominioDos
                );
    }

    @Test
    void listarDominios_filtroBusqueda_porEmailCliente() {

        prepararDominios();

        controller.listarDominios(
                "dos@cliente.com",
                null,
                null,
                null,
                null,
                null,
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(
                        dominioTres,
                        dominioCuatro
                );
    }

    @Test
    void listarDominios_filtroCliente() {
        ReflectionTestUtils.setField(clienteUno, "id", 1);
        ReflectionTestUtils.setField(clienteDos, "id", 2);

        dominioUno.setCliente(clienteUno);
        dominioDos.setCliente(clienteUno);
        dominioTres.setCliente(clienteDos);
        dominioCuatro.setCliente(clienteDos);

        when(gestionDominioService.obtenerTodosLosDominios())
                .thenReturn(List.of(
                        dominioUno,
                        dominioDos,
                        dominioTres,
                        dominioCuatro
                ));

        when(gestionDominioService.puedeMarcarComoRenovado(anyInt()))
                .thenReturn(false);

        String vista = controller.listarDominios(
                null,
                1,
                null,
                null,
                null,
                null,
                model
        );

        assertThat(vista).isEqualTo("gestion/dominios");

        List<Dominio> resultado = capturarUltimaListaDominios();

        assertThat(resultado)
                .containsExactly(dominioUno, dominioDos);
    }

    @Test
    void listarDominios_filtroEstado() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                Estado.AVISO_ENVIADO,
                null,
                null,
                null,
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioTres);
    }

    @Test
    void listarDominios_filtroEstadoRenovacion() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                EstadoRenovacion.RENOVADO,
                null,
                null,
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioDos);
    }

    @Test
    void listarDominios_filtroRegistrador() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                Registrador.NOMINALIA,
                null,
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioTres);
    }

    @Test
    void listarDominios_filtroExpiracion7() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                "7",
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioUno);
    }

    @Test
    void listarDominios_filtroExpiracion30() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                "30",
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(
                        dominioUno,
                        dominioDos
                );
    }

    @Test
    void listarDominios_filtroExpirados() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                "expirados",
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(dominioCuatro);
    }

    @Test
    void listarDominios_expiracionDesconocida_noFiltra() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                "999",
                model
        );

        List<Dominio> filtrados =
                capturarUltimaListaDominios();

        assertThat(filtrados)
                .containsExactly(
                        dominioUno,
                        dominioDos,
                        dominioTres,
                        dominioCuatro
                );
    }

    @Test
    void listarDominios_guardaEstadoDeLosFiltrosEnModelo() {

        prepararDominios();

        controller.listarDominios(
                "uno",
                clienteUno.getId(),
                Estado.ACTIVO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                "7",
                model
        );

        verify(model).addAttribute(
                "busqueda",
                "uno"
        );

        verify(model).addAttribute(
                "clienteId",
                clienteUno.getId()
        );

        verify(model).addAttribute(
                "estado",
                Estado.ACTIVO
        );

        verify(model).addAttribute(
                "estadoRenovacion",
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        verify(model).addAttribute(
                "registrador",
                Registrador.DOMITECA
        );

        verify(model).addAttribute(
                "expiracion",
                "7"
        );
    }

    @Test
    void listarDominios_calculaCorrectamenteDominiosRenovables() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                null,
                model
        );

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(model).addAttribute(
                eq("dominiosRenovables"),
                captor.capture()
        );

        @SuppressWarnings("unchecked")
        java.util.Map<Integer, Boolean> renovables =
                (java.util.Map<Integer, Boolean>)
                        captor.getValue();

        assertThat(renovables)
                .containsEntry(1, true)
                .containsEntry(2, false)
                .containsEntry(3, false)
                .containsEntry(4, false);
    }

    @Test
    void listarDominios_clientesSeOrdenanPorNombre() {

        prepararDominios();

        controller.listarDominios(
                null,
                null,
                null,
                null,
                null,
                null,
                model
        );

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(model).addAttribute(
                eq("clientes"),
                captor.capture()
        );

        @SuppressWarnings("unchecked")
        List<Cliente> clientes =
                (List<Cliente>) captor.getValue();

        assertThat(clientes)
                .containsExactly(
                        clienteDos,
                        clienteUno
                );
    }

    // =========================================================
    // GET /dominios/{id}
    // =========================================================

    @Test
    void detalleDominio_cargaTodosLosDatosYDevuelveVista() {

        HistorialDominio historial =
                new HistorialDominio();

        Facturacion facturacion =
                new Facturacion();

        when(gestionDominioService.obtenerDominio(1))
                .thenReturn(dominioUno);

        when(gestionDominioService.obtenerHistorial(1))
                .thenReturn(List.of(historial));

        when(gestionDominioService.obtenerFacturacion(1))
                .thenReturn(Optional.of(facturacion));

        when(gestionDominioService.puedeMarcarComoRenovado(1))
                .thenReturn(true);

        String resultado =
                controller.detalleDominio(1, model);

        assertThat(resultado)
                .isEqualTo(
                        "gestion/dominio-detalle"
                );

        verify(model).addAttribute(
                "dominio",
                dominioUno
        );

        verify(model).addAttribute(
                "historial",
                List.of(historial)
        );

        verify(model).addAttribute(
                "facturacion",
                facturacion
        );

        verify(model).addAttribute(
                "puedeRenovar",
                true
        );
    }

    @Test
    void detalleDominio_sinFacturacion_pasaNullAlModelo() {

        when(gestionDominioService.obtenerDominio(1))
                .thenReturn(dominioUno);

        when(gestionDominioService.obtenerHistorial(1))
                .thenReturn(List.of());

        when(gestionDominioService.obtenerFacturacion(1))
                .thenReturn(Optional.empty());

        when(gestionDominioService.puedeMarcarComoRenovado(1))
                .thenReturn(false);

        String resultado =
                controller.detalleDominio(1, model);

        assertThat(resultado)
                .isEqualTo(
                        "gestion/dominio-detalle"
                );

        verify(model).addAttribute(
                "facturacion",
                null
        );

        verify(model).addAttribute(
                "puedeRenovar",
                false
        );
    }

    // =========================================================
    // POST /dominios/{id}/facturar
    // =========================================================

    @Test
    void marcarComoFacturado_llamaAlServicioYRedirigeAlDetalle() {

        String resultado =
                controller.marcarComoFacturado(2);

        assertThat(resultado)
                .isEqualTo(
                        "redirect:/gestion/dominios/2"
                );

        verify(facturacionService)
                .marcarComoFacturado(2);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void prepararDominios() {

        List<Dominio> dominios = List.of(
                dominioUno,
                dominioDos,
                dominioTres,
                dominioCuatro
        );

        when(gestionDominioService.obtenerTodosLosDominios())
                .thenReturn(dominios);

        when(gestionDominioService.puedeMarcarComoRenovado(1))
                .thenReturn(true);

        when(gestionDominioService.puedeMarcarComoRenovado(2))
                .thenReturn(false);

        when(gestionDominioService.puedeMarcarComoRenovado(3))
                .thenReturn(false);

        when(gestionDominioService.puedeMarcarComoRenovado(4))
                .thenReturn(false);
    }

    @SuppressWarnings("unchecked")
    private List<Dominio> capturarUltimaListaDominios() {

        ArgumentCaptor<List<Dominio>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(model, atLeast(1))
                .addAttribute(
                        eq("dominios"),
                        captor.capture()
                );

        List<List<Dominio>> valores =
                captor.getAllValues();

        return valores.get(valores.size() - 1);
    }

    private Dominio crearDominio(
            int id,
            String nombre,
            Cliente cliente,
            Estado estado,
            EstadoRenovacion estadoRenovacion,
            Registrador registrador,
            LocalDate fechaExpiracion
    ) {

        Dominio dominio = new Dominio();

        dominio.setId(id);
        dominio.setNombreDominio(nombre);
        dominio.setCliente(cliente);
        dominio.setEstado(estado);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(registrador);
        dominio.setFechaExpiracion(fechaExpiracion);

        return dominio;
    }
}
