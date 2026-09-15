package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.service.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller para la interaccion de la renovacion con el cliente
 *
 */
@Controller
@RequestMapping("${app.url.renovacion}")
public class RenovacionController {

    private final TokenService tokenService;

    public RenovacionController(TokenService tokenService1) {
        this.tokenService = tokenService1;
    }

    /**
     * GET informacion y validar token para mostrar página de renovacion al cliente
     *
     * @param token
     * @param model
     * @return
     */
    @GetMapping
    public String mostrarFormulario(@RequestParam String token, Model model) {

        ResultadoValidacionRec resultadoValidacionRec = tokenService.validarToken(token);

        // Se añaden los datos en el model
        model.addAttribute("resultado", resultadoValidacionRec.resultado());

        // Se rellenan los campos de la vista en función del resultado
        switch (resultadoValidacionRec.resultado()) {
            case VALIDO -> {
                //necesito cliente y dominios asociados al token
                model.addAttribute("cliente", resultadoValidacionRec.token().getCliente());
                model.addAttribute("dominios", resultadoValidacionRec.token().getCliente().getDominios());
                model.addAttribute("token", resultadoValidacionRec.token().getToken());
            }
            case USADO, EXPIRADO, NO_ENCONTRADO -> {
                //
            }
        }
        return "web/renovacion-confirmacion-cliente";
    }

    /**
     * POST al enviar el formulario que confirma la renovacion de uno o varios dominios
     * Vuelve a validar el token y marca como usado el token si el cliente ha terminado el formulario
     * en RequestParam required es false para qye sea null y no lance excepcion
     */
    @PostMapping("/enviar")
    public String enviarFormulario(@RequestParam(name = "dominios", required = false) List<Integer> idsDominiosMarcados,
                                   String token, Model model) {

        // Inicialización del requestParam
        if (idsDominiosMarcados == null) {
            idsDominiosMarcados = new ArrayList<>();
        }
        ResultadoValidacionRec resultadoValidacionRec = tokenService.validarToken(token);

        model.addAttribute("resultado", resultadoValidacionRec.resultado());

        switch (resultadoValidacionRec.resultado()) {
            case EXPIRADO, USADO, NO_ENCONTRADO -> {
                /**/
            }
            case VALIDO -> {
                tokenService.procesarConfirmacion(resultadoValidacionRec.token(), idsDominiosMarcados);
            }
        }

        return "web/renovacion-correcta";

    }
}
