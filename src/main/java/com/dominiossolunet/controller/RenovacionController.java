package com.dominiossolunet.controller;

import org.springframework.ui.Model;
import com.dominiossolunet.dto.ResultadoValidacion;
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

        ResultadoValidacion resultadoValidacion = tokenService.validarToken(token);
        
        // Se añaden los datos en el model
        model.addAttribute("resultado", resultadoValidacion.resultado());

        // Se rellenan los campos de la vista en función del resultado
        switch (resultadoValidacion.resultado()) {
            case VALIDO -> {
                //necesito cliente y dominios asociados al token
                model.addAttribute("cliente", resultadoValidacion.token().getCliente());
                model.addAttribute("dominios", resultadoValidacion.token().getCliente().getDominios());
                model.addAttribute("token", resultadoValidacion.token().getToken());
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
