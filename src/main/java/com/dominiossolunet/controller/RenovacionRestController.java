package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ConfirmacionRenovacionRequest;
import com.dominiossolunet.dto.RenovacionDominioResponse;
import com.dominiossolunet.dto.RenovacionTokenResponse;
import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.service.TokenService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renovaciones")
public class RenovacionRestController {

    private final TokenService tokenService;

    public RenovacionRestController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/validar")
    public RenovacionTokenResponse validarToken(@RequestParam String token) {
        ResultadoValidacionRec validacion = tokenService.validarToken(token);

        if (validacion.resultado() != ResultadoValidacion.VALIDO) {
            return new RenovacionTokenResponse(
                    validacion.resultado(),
                    null,
                    List.of()
            );
        }

        List<RenovacionDominioResponse> dominios =
                tokenService.obtenerTokenDominioPorTokenCliente(validacion.token())
                        .stream()
                        .map(TokenDominio::getDominio)
                        .map(d -> new RenovacionDominioResponse(
                                d.getId(),
                                d.getNombreDominio(),
                                d.getFechaExpiracion()
                        ))
                        .toList();

        return new RenovacionTokenResponse(
                ResultadoValidacion.VALIDO,
                validacion.token().getCliente().getNombre(),
                dominios
        );
    }

    @PostMapping("/confirmar")
    public String confirmarRenovacion(
            @RequestBody ConfirmacionRenovacionRequest request
    ) {
        ResultadoValidacionRec validacion =
                tokenService.validarToken(request.token());

        if (validacion.resultado() != ResultadoValidacion.VALIDO) {
            return validacion.resultado().name();
        }

        List<Integer> idsDominios = request.dominios() == null
                ? List.of()
                : request.dominios();

        tokenService.procesarConfirmacion(validacion.token(), idsDominios);

        return "PROCESADA";
    }
}