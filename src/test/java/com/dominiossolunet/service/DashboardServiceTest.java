package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    private GestionDominioService gestionDominioService;
    private DashboardService dashboardService;

    private List<Dominio> dominios;

    @BeforeEach
    void setUp() {

        gestionDominioService = mock(GestionDominioService.class);

        dashboardService =
                new DashboardService(gestionDominioService);

        dominios = new ArrayList<>();

        dominios.add(crearDominio(
                1,
                "activo.es",
                Estado.ACTIVO,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(60)
        ));

        dominios.add(crearDominio(
                2,
                "pendiente.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        ));

        dominios.add(crearDominio(
                3,
                "renovado.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(20)
        ));

        dominios.add(crearDominio(
                4,
                "rechazado.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.RECHAZADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(40)
        ));

        dominios.add(crearDominio(
                5,
                "expirado.es",
                Estado.EXPIRADO_SIN_RESPUESTA,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.NOMINALIA,
                LocalDate.now().minusDays(3)
        ));
    }

    private Dominio crearDominio(
            int id,
            String nombre,
            Estado estado,
            EstadoRenovacion estadoRenovacion,
            Registrador registrador,
            LocalDate fechaExpiracion) {

        Dominio dominio = new Dominio();

        dominio.setId(id);
        dominio.setNombreDominio(nombre);
        dominio.setEstado(estado);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(registrador);
        dominio.setFechaExpiracion(fechaExpiracion);

        return dominio;
    }

    @Test
    void obtenerDominios_delegaEnGestionDominioService() {

        when(gestionDominioService.obtenerTodosLosDominios())
                .thenReturn(dominios);

        List<Dominio> resultado =
                dashboardService.obtenerDominios();

        assertThat(resultado)
                .isSameAs(dominios);

        verify(gestionDominioService)
                .obtenerTodosLosDominios();
    }

    @Test
    void totalDominios_devuelveNumeroCorrecto() {

        assertThat(
                dashboardService.totalDominios(dominios)
        ).isEqualTo(5);
    }

    @Test
    void dominiosActivos_cuentaSoloLosActivos() {

        assertThat(
                dashboardService.dominiosActivos(dominios)
        ).isEqualTo(2);
    }

    @Test
    void pendientesRenovacion_cuentaPendientes() {

        assertThat(
                dashboardService.pendientesRenovacion(dominios)
        ).isEqualTo(1);
    }

    @Test
    void renovacionesRealizadas_cuentaRenovados() {

        assertThat(
                dashboardService.renovacionesRealizadas(dominios)
        ).isEqualTo(1);
    }

    @Test
    void renovacionesRechazadas_cuentaRechazados() {

        assertThat(
                dashboardService.renovacionesRechazadas(dominios)
        ).isEqualTo(1);
    }

    @Test
    void expirados_cuentaDominiosExpirados() {

        assertThat(
                dashboardService.expirados(dominios)
        ).isEqualTo(1);
    }

    @Test
    void expiranEn30Dias_cuentaSoloLosQueEstanDentroDelLimite() {

        assertThat(
                dashboardService.expiranEn30Dias(dominios)
        ).isEqualTo(2);
    }

    @Test
    void expiranEn30Dias_noCuentaDominiosExpirados() {

        List<Dominio> datos = List.of(
                crearDominio(
                        10,
                        "expirado.es",
                        Estado.EXPIRADO_SIN_RESPUESTA,
                        EstadoRenovacion.SIN_RENOVACION,
                        Registrador.DOMITECA,
                        LocalDate.now().minusDays(1)
                )
        );

        assertThat(
                dashboardService.expiranEn30Dias(datos)
        ).isZero();
    }

    @Test
    void expiranEn30Dias_noCuentaDominiosPosterioresA30Dias() {

        List<Dominio> datos = List.of(
                crearDominio(
                        10,
                        "lejano.es",
                        Estado.ACTIVO,
                        EstadoRenovacion.SIN_RENOVACION,
                        Registrador.DOMITECA,
                        LocalDate.now().plusDays(31)
                )
        );

        assertThat(
                dashboardService.expiranEn30Dias(datos)
        ).isZero();
    }

    @Test
    void obtenerProximasExpiraciones_devuelveOrdenadasPorFecha() {

        List<Dominio> resultado =
                dashboardService.obtenerProximasExpiraciones(dominios);

        assertThat(resultado)
                .extracting(Dominio::getNombreDominio)
                .containsExactly(
                        "pendiente.es",
                        "renovado.es"
                );
    }

    @Test
    void obtenerProximasExpiraciones_noIncluyeExpirados() {

        List<Dominio> resultado =
                dashboardService.obtenerProximasExpiraciones(dominios);

        assertThat(resultado)
                .extracting(Dominio::getNombreDominio)
                .doesNotContain("expirado.es");
    }

    @Test
    void obtenerProximasExpiraciones_noIncluyeDominiosFueraDe30Dias() {

        List<Dominio> resultado =
                dashboardService.obtenerProximasExpiraciones(dominios);

        assertThat(resultado)
                .extracting(Dominio::getNombreDominio)
                .doesNotContain("activo.es");
    }

    @Test
    void obtenerProximasExpiraciones_limitaResultadoA10() {

        List<Dominio> muchosDominios = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {

            muchosDominios.add(crearDominio(
                    i,
                    "dominio" + i + ".es",
                    Estado.ACTIVO,
                    EstadoRenovacion.SIN_RENOVACION,
                    Registrador.DOMITECA,
                    LocalDate.now().plusDays(i)
            ));
        }

        List<Dominio> resultado =
                dashboardService.obtenerProximasExpiraciones(
                        muchosDominios
                );

        assertThat(resultado)
                .hasSize(10);
    }

    @Test
    void dominiosPorRegistrador_agrupaCorrectamente() {

        Map<Registrador, Long> resultado =
                dashboardService.dominiosPorRegistrador(dominios);

        assertThat(resultado)
                .containsEntry(Registrador.DOMITECA, 2L)
                .containsEntry(Registrador.GANDI, 2L)
                .containsEntry(Registrador.NOMINALIA, 1L);
    }

    @Test
    void dominiosPorRegistrador_noIncluyeRegistradoresSinDominios() {

        Map<Registrador, Long> resultado =
                dashboardService.dominiosPorRegistrador(dominios);

        assertThat(resultado)
                .doesNotContainKey(Registrador.OTRO);
    }

    @Test
    void dominiosPorRegistrador_ignoraRegistradorNull() {

        Dominio dominioSinRegistrador = crearDominio(
                20,
                "sin-registrador.es",
                Estado.ACTIVO,
                EstadoRenovacion.SIN_RENOVACION,
                null,
                LocalDate.now().plusDays(10)
        );

        List<Dominio> datos = new ArrayList<>(dominios);
        datos.add(dominioSinRegistrador);

        Map<Registrador, Long> resultado =
                dashboardService.dominiosPorRegistrador(datos);

        assertThat(resultado.values())
                .allMatch(numero -> numero > 0);

        assertThat(resultado)
                .containsEntry(Registrador.DOMITECA, 2L)
                .containsEntry(Registrador.GANDI, 2L)
                .containsEntry(Registrador.NOMINALIA, 1L);
    }

    @Test
    void metodosDeConteo_conListaVacia_devuelvenCero() {

        List<Dominio> vacio = List.of();

        assertThat(
                dashboardService.totalDominios(vacio)
        ).isZero();

        assertThat(
                dashboardService.dominiosActivos(vacio)
        ).isZero();

        assertThat(
                dashboardService.pendientesRenovacion(vacio)
        ).isZero();

        assertThat(
                dashboardService.renovacionesRealizadas(vacio)
        ).isZero();

        assertThat(
                dashboardService.renovacionesRechazadas(vacio)
        ).isZero();

        assertThat(
                dashboardService.expirados(vacio)
        ).isZero();

        assertThat(
                dashboardService.expiranEn30Dias(vacio)
        ).isZero();

        assertThat(
                dashboardService.obtenerProximasExpiraciones(vacio)
        ).isEmpty();

        assertThat(
                dashboardService.dominiosPorRegistrador(vacio)
        ).isEmpty();
    }
}
