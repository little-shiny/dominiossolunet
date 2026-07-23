package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.repository.TokenRenovacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Métodos a implementar: generar un nuevo token, validarlo(que existe, que no ha caducado, que no está usado
 *
 */
@Service
public class TokenService {

    private final TokenRenovacionRepository tokenRenovacionRepository; //final porque va en el constructor y es unico

    @Value("${token.dias-expiracion}")
    private int diasExpiracionToken;

    //Constructor
    public TokenService(TokenRenovacionRepository tokenRenovacionRepository) {
        this.tokenRenovacionRepository = tokenRenovacionRepository;
    }

    public TokenCliente generarToken(Dominio dominio){
        TokenCliente nuevoToken = new TokenCliente();

        nuevoToken.setFechaCreacion(LocalDateTime.now());
        nuevoToken.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracionToken));
        nuevoToken.setToken(UUID.randomUUID().toString());
        nuevoToken.setUsado(false);

        return tokenRenovacionRepository.save(nuevoToken);
    }

    public ResultadoValidacionRec validarToken(String token){

        Optional<TokenCliente> resultado = tokenRenovacionRepository.findByToken(token);
        TokenCliente tokenEncontrado;

        if (resultado.isPresent()){
            tokenEncontrado = resultado.get();

            if (tokenEncontrado.isUsado()) {
                return new ResultadoValidacionRec(ResultadoValidacion.USADO, tokenEncontrado);
            } else if (tokenEncontrado.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                return new ResultadoValidacionRec(ResultadoValidacion.EXPIRADO, tokenEncontrado);
            }
            return new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenEncontrado);

        } else {
            return new ResultadoValidacionRec(ResultadoValidacion.NO_ENCONTRADO, null);
        }
    }
}

