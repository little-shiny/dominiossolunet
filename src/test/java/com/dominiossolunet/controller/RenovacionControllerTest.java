package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenovacionControllerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private Model model;

    private RenovacionController controller;

    @BeforeEach
    void setUp() {
        controller = new RenovacionController(tokenService);
    }

    @Test
    void enviarFormulario_tokenValido_todosConfirmados_procesaTodosLosDominios() {

        // ARRANGE
        String token = "token-valido";
        List<Integer> idsDominios = List.of(1, 2, 3);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                idsDominios,
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.VALIDO
        );

        verify(tokenService).validarToken(token);

        verify(tokenService).procesarConfirmacion(
                tokenCliente,
                idsDominios
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void enviarFormulario_tokenValido_algunosConfirmados_procesaSoloLosSeleccionados() {

        // ARRANGE
        String token = "token-valido";
        List<Integer> idsDominios = List.of(1, 3);

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                idsDominios,
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(tokenService).validarToken(token);

        verify(tokenService).procesarConfirmacion(
                tokenCliente,
                idsDominios
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void enviarFormulario_tokenValido_ningunoConfirmado_procesaListaVacia() {

        // ARRANGE
        String token = "token-valido";

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                null,
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(tokenService).validarToken(token);

        verify(tokenService).procesarConfirmacion(
                tokenCliente,
                List.of()
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void enviarFormulario_tokenUsado_noProcesaConfirmacion() {

        // ARRANGE
        String token = "token-usado";

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.USADO,
                        null
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                List.of(1, 2),
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.USADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void enviarFormulario_tokenExpirado_noProcesaConfirmacion() {

        // ARRANGE
        String token = "token-expirado";

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.EXPIRADO,
                        null
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                List.of(1, 2),
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.EXPIRADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());
    }

    @Test
    void enviarFormulario_tokenNoEncontrado_noProcesaConfirmacion() {

        // ARRANGE
        String token = "token-inexistente";

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.NO_ENCONTRADO,
                        null
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.enviarFormulario(
                List.of(1, 2),
                token,
                model
        );

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-correcta");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.NO_ENCONTRADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());
    }

    @Test
    void mostrarFormulario_tokenValido_devuelveVistaConDatosCliente() {

        // ARRANGE
        String token = "token-valido";

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        Dominio dominio1 = new Dominio();
        dominio1.setNombreDominio("ana.com");

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("ana.es");

        cliente.setDominios(List.of(dominio1, dominio2));

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);
        tokenCliente.setCliente(cliente);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.VALIDO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.mostrarFormulario(token, model);

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-confirmacion-cliente");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                "VALIDO"
        );

        verify(model).addAttribute(
                "cliente",
                cliente
        );

        verify(model).addAttribute(
                "dominios",
                cliente.getDominios()
        );

        verify(model).addAttribute(
                "token",
                token
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void mostrarFormulario_tokenUsado_devuelveVistaSinDatosCliente() {

        // ARRANGE
        String token = "token-usado";

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.USADO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.mostrarFormulario(token, model);

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-confirmacion-cliente");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                "USADO"
        );

        verify(model, never()).addAttribute(
                eq("cliente"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("dominios"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("token"),
                any()
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void mostrarFormulario_tokenExpirado_devuelveVistaSinDatosCliente() {

        // ARRANGE
        String token = "token-expirado";

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken(token);

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.EXPIRADO,
                        tokenCliente
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.mostrarFormulario(token, model);

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-confirmacion-cliente");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                "EXPIRADO"
        );

        verify(model, never()).addAttribute(
                eq("cliente"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("dominios"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("token"),
                any()
        );

        verifyNoMoreInteractions(tokenService);
    }

    @Test
    void mostrarFormulario_tokenNoEncontrado_devuelveVistaSinDatosCliente() {

        // ARRANGE
        String token = "token-inexistente";

        ResultadoValidacionRec resultado =
                new ResultadoValidacionRec(
                        ResultadoValidacion.NO_ENCONTRADO,
                        null
                );

        when(tokenService.validarToken(token))
                .thenReturn(resultado);

        // ACT
        String vista = controller.mostrarFormulario(token, model);

        // ASSERT
        assertThat(vista)
                .isEqualTo("web/renovacion-confirmacion-cliente");

        verify(tokenService).validarToken(token);

        verify(model).addAttribute(
                "resultado",
                "NO_ENCONTRADO"
        );

        verify(model, never()).addAttribute(
                eq("cliente"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("dominios"),
                any()
        );

        verify(model, never()).addAttribute(
                eq("token"),
                any()
        );

        verifyNoMoreInteractions(tokenService);
    }
}