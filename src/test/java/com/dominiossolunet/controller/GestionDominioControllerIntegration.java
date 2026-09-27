package com.dominiossolunet.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
class GestionDominioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private HistorialDominioRepository historialDominioRepository;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {

        /*
         * Hay que respetar las relaciones de las FK.
         */
        historialDominioRepository.deleteAll();
        tokenDominioRepository.deleteAll();
        facturacionRepository.deleteAll();
        tokenClienteRepository.deleteAll();
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // =========================================================
    // GET /gestion/dominios
    // =========================================================

    @Test
    void listarDominios_muestraLosDominiosExistentes() throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Integracion",
                "integracion@cliente.com"
        );

        crearDominio(
                cliente,
                "integracion-uno.es",
                Estado.ACTIVO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        crearDominio(
                cliente,
                "integracion-dos.com",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(
                        get("/gestion/dominios")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominios"))
                .andExpect(content().string(
                        containsString("integracion-uno.es")
                ))
                .andExpect(content().string(
                        containsString("integracion-dos.com")
                ))
                .andExpect(content().string(
                        containsString("Cliente Integracion")
                ))
                .andExpect(content().string(
                        containsString("integracion@cliente.com")
                ))
                .andExpect(content().string(
                        containsString("2 dominios encontrados")
                ));
    }

    @Test
    void listarDominios_filtroBusqueda_soloMuestraCoincidencias()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Filtro",
                "filtro@cliente.com"
        );

        crearDominio(
                cliente,
                "buscar-este.es",
                Estado.ACTIVO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(10)
        );

        crearDominio(
                cliente,
                "no-aparece.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(20)
        );

        mockMvc.perform(
                        get("/gestion/dominios")
                                .param("busqueda", "buscar-este")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominios"))
                .andExpect(content().string(
                        containsString("buscar-este.es")
                ))
                .andExpect(content().string(
                        not(containsString("no-aparece.es"))
                ))
                .andExpect(content().string(
                        containsString("1 dominios encontrados")
                ));
    }

    @Test
    void listarDominios_filtroPorEstadoSoloMuestraCoincidencias()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Estado",
                "estado@cliente.com"
        );

        crearDominio(
                cliente,
                "activo.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(20)
        );

        crearDominio(
                cliente,
                "aviso.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.GANDI,
                LocalDate.now().plusDays(5)
        );

        mockMvc.perform(
                        get("/gestion/dominios")
                                .param("estado", "AVISO_ENVIADO")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominios"))
                .andExpect(content().string(
                        containsString("aviso.es")
                ))
                .andExpect(content().string(
                        not(containsString("activo.es"))
                ))
                .andExpect(content().string(
                        containsString("1 dominios encontrados")
                ));
    }

    @Test
    void listarDominios_filtroPorExpiracion7_muestraDominioCorrecto()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Expiracion",
                "expiracion@cliente.com"
        );

        crearDominio(
                cliente,
                "expira-pronto.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        crearDominio(
                cliente,
                "expira-lejos.es",
                Estado.ACTIVO,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.GANDI,
                LocalDate.now().plusDays(20)
        );

        mockMvc.perform(
                        get("/gestion/dominios")
                                .param("expiracion", "7")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominios"))
                .andExpect(content().string(
                        containsString("expira-pronto.es")
                ))
                .andExpect(content().string(
                        not(containsString("expira-lejos.es"))
                ))
                .andExpect(content().string(
                        containsString("1 dominios encontrados")
                ));
    }

    @Test
    void listarDominios_filtroExpirados_muestraSoloExpirados()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Expirado",
                "expirado@cliente.com"
        );

        crearDominio(
                cliente,
                "ya-expirado.es",
                Estado.EXPIRADO_SIN_RESPUESTA,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.OTRO,
                LocalDate.now().minusDays(5)
        );

        crearDominio(
                cliente,
                "todavia-activo.es",
                Estado.ACTIVO,
                EstadoRenovacion.SIN_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        mockMvc.perform(
                        get("/gestion/dominios")
                                .param("expiracion", "expirados")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominios"))
                .andExpect(content().string(
                        containsString("ya-expirado.es")
                ))
                .andExpect(content().string(
                        not(containsString("todavia-activo.es"))
                ))
                .andExpect(content().string(
                        containsString("1 dominios encontrados")
                ));
    }

    // =========================================================
    // GET /gestion/dominios/{id}
    // =========================================================

    @Test
    void detalleDominio_muestraInformacionDelDominio()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Detalle",
                "detalle@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "detalle-integracion.es",
                Estado.ACTIVO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(
                        get("/gestion/dominios/{id}", dominio.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominio-detalle"))
                .andExpect(content().string(
                        containsString("detalle-integracion.es")
                ))
                .andExpect(content().string(
                        containsString("Cliente Detalle")
                ))
                .andExpect(content().string(
                        containsString("detalle@cliente.com")
                ))
                .andExpect(content().string(
                        containsString("DOMITECA")
                ))
                .andExpect(content().string(
                        containsString("PENDIENTE_RENOVACION")
                ))
                .andExpect(content().string(
                        containsString("SIN REGISTRO")
                ))
                .andExpect(content().string(
                        containsString(
                                "Este dominio todavía no tiene eventos registrados."
                        )
                ));
    }

    @Test
    void detalleDominio_conFacturacionMuestraEstadoDeFacturacion()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Facturacion",
                "facturacion@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "facturacion-integracion.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(30)
        );

        crearFacturacion(
                dominio,
                EstadoFacturacion.PENDIENTE_FACTURAR
        );

        mockMvc.perform(
                        get("/gestion/dominios/{id}", dominio.getId())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/dominio-detalle"))
                .andExpect(content().string(
                        containsString("facturacion-integracion.es")
                ))
                .andExpect(content().string(
                        containsString("PENDIENTE_FACTURAR")
                ));
    }

    // =========================================================
    // POST /gestion/dominios/{id}/renovar
    // =========================================================

    @Test
    void marcarComoRenovado_desdeHttp_actualizaDominioYGuardaHistorial()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Renovacion HTTP",
                "renovacion-http@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "renovacion-http.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        TokenCliente tokenCliente =
                crearTokenCliente(cliente);

        crearTokenDominioConfirmado(
                tokenCliente,
                dominio
        );

        mockMvc.perform(
                        post(
                                "/gestion/dominios/{id}/renovar",
                                dominio.getId()
                        )
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/gestion/dominios"
                ));

        Dominio dominioActualizado =
                dominioRepository
                        .findById(dominio.getId())
                        .orElseThrow();

        assertThat(
                dominioActualizado.getEstadoRenovacion()
        ).isEqualTo(
                EstadoRenovacion.RENOVADO
        );

        List<HistorialDominio> historial =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioActualizado
                        );

        assertThat(historial)
                .hasSize(1);

        assertThat(
                historial.getFirst().getTipoEvento()
        ).isEqualTo(
                TipoEventoDominio.RENOVACION_REALIZADA
        );

        assertThat(
                historial.getFirst().getDetalle()
        ).isEqualTo(
                "Renovación realizada en el registrador"
        );
    }

    @Test
    void marcarComoRenovado_sinConfirmacion_devuelveError()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Sin Confirmar",
                "sin-confirmar@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "sin-confirmar.es",
                Estado.AVISO_ENVIADO,
                EstadoRenovacion.PENDIENTE_RENOVACION,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(5)
        );

        TokenCliente tokenCliente =
                crearTokenCliente(cliente);

        TokenDominio tokenDominio =
                new TokenDominio();

        tokenDominio.setTokenCliente(tokenCliente);
        tokenDominio.setDominio(dominio);
        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.PENDIENTE
        );

        tokenDominioRepository.saveAndFlush(
                tokenDominio
        );

        /*
         * El servicio lanza IllegalStateException.
         * MockMvc la propaga como excepción del request.
         */
        try {

            mockMvc.perform(
                    post(
                            "/gestion/dominios/{id}/renovar",
                            dominio.getId()
                    )
            );

        } catch (Exception exception) {

            Throwable causa = exception;

            boolean encontrada = false;

            while (causa != null) {

                if (causa instanceof IllegalStateException) {

                    encontrada = true;

                    assertThat(causa.getMessage())
                            .contains(
                                    "El cliente no ha confirmado"
                            );

                    break;
                }

                causa = causa.getCause();
            }

            assertThat(encontrada)
                    .as("Se esperaba IllegalStateException")
                    .isTrue();
        }

        Dominio dominioSinCambios =
                dominioRepository
                        .findById(dominio.getId())
                        .orElseThrow();

        assertThat(
                dominioSinCambios.getEstadoRenovacion()
        ).isEqualTo(
                EstadoRenovacion.PENDIENTE_RENOVACION
        );

        assertThat(
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioSinCambios
                        )
        ).isEmpty();
    }

    // =========================================================
    // POST /gestion/dominios/{id}/facturar
    // =========================================================

    @Test
    void marcarComoFacturado_desdeHttp_actualizaFacturacionYGuardaHistorial()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Facturacion HTTP",
                "facturacion-http@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "facturacion-http.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.GANDI,
                LocalDate.now().plusDays(30)
        );

        Facturacion facturacion =
                crearFacturacion(
                        dominio,
                        EstadoFacturacion.PENDIENTE_FACTURAR
                );

        mockMvc.perform(
                        post(
                                "/gestion/dominios/{id}/facturar",
                                dominio.getId()
                        )
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/gestion/dominios/" + dominio.getId()
                ));

        Facturacion facturacionActualizada =
                facturacionRepository
                        .findById(facturacion.getId())
                        .orElseThrow();

        assertThat(
                facturacionActualizada.getEstadoFacturacion()
        ).isEqualTo(
                EstadoFacturacion.FACTURADO
        );

        assertThat(
                facturacionActualizada.getFechaUltimaFactura()
        ).isEqualTo(
                LocalDate.now()
        );

        List<HistorialDominio> historial =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominio
                        );

        assertThat(historial)
                .hasSize(1);

        assertThat(
                historial.getFirst().getTipoEvento()
        ).isEqualTo(
                TipoEventoDominio.FACTURACION_REALIZADA
        );

        assertThat(
                historial.getFirst().getDetalle()
        ).isEqualTo(
                "Facturación realizada"
        );
    }

    @Test
    void marcarComoFacturado_sinRegistroDeFacturacion_devuelveError()
            throws Exception {

        Cliente cliente = crearCliente(
                "Cliente Sin Facturacion",
                "sin-facturacion@cliente.com"
        );

        Dominio dominio = crearDominio(
                cliente,
                "sin-facturacion.es",
                Estado.ACTIVO,
                EstadoRenovacion.RENOVADO,
                Registrador.DOMITECA,
                LocalDate.now().plusDays(30)
        );

        try {

            mockMvc.perform(
                    post(
                            "/gestion/dominios/{id}/facturar",
                            dominio.getId()
                    )
            );

        } catch (Exception exception) {

            Throwable causa = exception;

            boolean encontrada = false;

            while (causa != null) {

                if (causa instanceof IllegalStateException) {

                    encontrada = true;

                    assertThat(causa.getMessage())
                            .contains(
                                    "no tiene un registro de facturación"
                            );

                    break;
                }

                causa = causa.getCause();
            }

            assertThat(encontrada)
                    .as("Se esperaba IllegalStateException")
                    .isTrue();
        }

        assertThat(
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominio
                        )
        ).isEmpty();
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
            String nombre,
            Estado estado,
            EstadoRenovacion estadoRenovacion,
            Registrador registrador,
            LocalDate fechaExpiracion
    ) {

        Dominio dominio = new Dominio();

        dominio.setCliente(cliente);
        dominio.setNombreDominio(nombre);
        dominio.setEstado(estado);
        dominio.setEstadoRenovacion(estadoRenovacion);
        dominio.setRegistrador(registrador);
        dominio.setFechaExpiracion(fechaExpiracion);

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

    private TokenDominio crearTokenDominioConfirmado(
            TokenCliente tokenCliente,
            Dominio dominio
    ) {

        TokenDominio tokenDominio =
                new TokenDominio();

        tokenDominio.setTokenCliente(
                tokenCliente
        );

        tokenDominio.setDominio(
                dominio
        );

        tokenDominio.setEstadoAvisoRenovacion(
                EstadoAvisoRenovacion.CONFIRMADO
        );

        tokenDominio.setFechaInteraccionCliente(
                LocalDateTime.now()
        );

        return tokenDominioRepository.saveAndFlush(
                tokenDominio
        );
    }

    private Facturacion crearFacturacion(
            Dominio dominio,
            EstadoFacturacion estado
    ) {

        Facturacion facturacion =
                new Facturacion();

        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(estado);

        return facturacionRepository.saveAndFlush(
                facturacion
        );
    }
}
