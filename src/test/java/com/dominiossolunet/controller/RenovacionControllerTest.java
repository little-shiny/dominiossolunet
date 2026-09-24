package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para RenovacionController.
 */
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

    // ============================================================
    // mostrarFormulario()
    // ============================================================

    @Test
    void mostrarFormulario_tokenValido_devuelveVistaConDatosCliente() {

        // Arrange
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("token-valido");
        tokenCliente.setCliente(cliente);

        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.VALIDO,
                tokenCliente
        );

        when(tokenService.validarToken("token-valido"))
                .thenReturn(resultado);

        // Act
        String vista = controller.mostrarFormulario("token-valido", model);

        // Assert
        assertEquals(
                "web/renovacion-confirmacion-cliente",
                vista
        );

        verify(tokenService).validarToken("token-valido");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.VALIDO
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
                "token-valido"
        );
    }

    @Test
    void mostrarFormulario_tokenUsado_noAnadeDatosCliente() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.USADO,
                null
        );

        when(tokenService.validarToken("token-usado"))
                .thenReturn(resultado);

        // Act
        String vista = controller.mostrarFormulario(
                "token-usado",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-confirmacion-cliente",
                vista
        );

        verify(tokenService).validarToken("token-usado");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.USADO
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
    }

    @Test
    void mostrarFormulario_tokenExpirado_noAnadeDatosCliente() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.EXPIRADO,
                null
        );

        when(tokenService.validarToken("token-expirado"))
                .thenReturn(resultado);

        // Act
        String vista = controller.mostrarFormulario(
                "token-expirado",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-confirmacion-cliente",
                vista
        );

        verify(tokenService).validarToken("token-expirado");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.EXPIRADO
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
    }

    @Test
    void mostrarFormulario_tokenNoEncontrado_noAnadeDatosCliente() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.NO_ENCONTRADO,
                null
        );

        when(tokenService.validarToken("token-inexistente"))
                .thenReturn(resultado);

        // Act
        String vista = controller.mostrarFormulario(
                "token-inexistente",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-confirmacion-cliente",
                vista
        );

        verify(tokenService).validarToken("token-inexistente");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.NO_ENCONTRADO
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
    }

    // ============================================================
    // enviarFormulario()
    // ============================================================

    @Test
    void enviarFormulario_tokenValido_conDominiosSeleccionados_procesaConfirmacion() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("token-valido");

        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.VALIDO,
                tokenCliente
        );

        List<Integer> dominios = List.of(1, 2, 3);

        when(tokenService.validarToken("token-valido"))
                .thenReturn(resultado);

        // Act
        String vista = controller.enviarFormulario(
                dominios,
                "token-valido",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-correcta",
                vista
        );

        verify(tokenService).validarToken("token-valido");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.VALIDO
        );

        verify(tokenService).procesarConfirmacion(
                tokenCliente,
                dominios
        );

        // Debe procesarse exactamente una vez
        verify(tokenService, org.mockito.Mockito.times(1))
                .procesarConfirmacion(
                        tokenCliente,
                        dominios
                );
    }

    @Test
    void enviarFormulario_tokenValido_sinDominios_procesaConfirmacionConListaVacia() {

        // Arrange
        TokenCliente tokenCliente = new TokenCliente();
        tokenCliente.setToken("token-valido");

        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.VALIDO,
                tokenCliente
        );

        when(tokenService.validarToken("token-valido"))
                .thenReturn(resultado);

        // Act
        String vista = controller.enviarFormulario(
                null,
                "token-valido",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-correcta",
                vista
        );

        verify(tokenService).validarToken("token-valido");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.VALIDO
        );

        verify(tokenService).procesarConfirmacion(
                eq(tokenCliente),
                argThat(lista ->
                        lista != null && lista.isEmpty()
                )
        );
    }

    @Test
    void enviarFormulario_tokenUsado_noProcesaConfirmacion() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.USADO,
                null
        );

        List<Integer> dominios = List.of(1, 2);

        when(tokenService.validarToken("token-usado"))
                .thenReturn(resultado);

        // Act
        String vista = controller.enviarFormulario(
                dominios,
                "token-usado",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-correcta",
                vista
        );

        verify(tokenService).validarToken("token-usado");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.USADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());
    }

    @Test
    void enviarFormulario_tokenExpirado_noProcesaConfirmacion() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.EXPIRADO,
                null
        );

        List<Integer> dominios = List.of(1, 2);

        when(tokenService.validarToken("token-expirado"))
                .thenReturn(resultado);

        // Act
        String vista = controller.enviarFormulario(
                dominios,
                "token-expirado",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-correcta",
                vista
        );

        verify(tokenService).validarToken("token-expirado");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.EXPIRADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());
    }

    @Test
    void enviarFormulario_tokenNoEncontrado_noProcesaConfirmacion() {

        // Arrange
        ResultadoValidacionRec resultado = new ResultadoValidacionRec(
                ResultadoValidacion.NO_ENCONTRADO,
                null
        );

        List<Integer> dominios = List.of(1, 2);

        when(tokenService.validarToken("token-inexistente"))
                .thenReturn(resultado);

        // Act
        String vista = controller.enviarFormulario(
                dominios,
                "token-inexistente",
                model
        );

        // Assert
        assertEquals(
                "web/renovacion-correcta",
                vista
        );

        verify(tokenService).validarToken("token-inexistente");

        verify(model).addAttribute(
                "resultado",
                ResultadoValidacion.NO_ENCONTRADO
        );

        verify(tokenService, never())
                .procesarConfirmacion(any(), any());
    }
}
