package com.dominiossolunet.controller;

import org.springframework.ui.Model;
import com.dominiossolunet.model.enums.ResultadoValidacionToken;
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
    public RenovacionController(TokenService tokenService1){
        this.tokenService = tokenService1;
    }

    //todo GET al pinchar en el enlace del mail y validar el token
    @GetMapping("${app.url.dominio}")
    public String mostrarFormulario(@RequestParam String token, Model model){

        ResultadoValidacionToken resultadoValidacionToken = tokenService.validarToken(token);
        // Verificar token
        
        // datos que se meten en el model
        // devolver el nombre de la vista
    }

    //todo POST confirmar
    //todo Post rechazar
}
