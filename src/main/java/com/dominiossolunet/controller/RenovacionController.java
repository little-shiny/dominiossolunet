package com.dominiossolunet.controller;

import org.springframework.ui.Model;
import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.service.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller para la interaccion de la renovacion con el cliente
 *
 */
@Controller
public class RenovacionController {

    private final TokenService tokenService;

    public RenovacionController(TokenService tokenService1) {
        this.tokenService = tokenService1;
    }

    @GetMapping("${app.url.dominio}")
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

    //todo POST confirmar
    //todo Post rechazar
}
