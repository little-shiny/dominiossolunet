package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.repository.TokenRenovacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        nuevoToken.setDominio(dominio);
        nuevoToken.setToken(UUID.randomUUID().toString());
        nuevoToken.setUsado(false);

        return tokenRenovacionRepository.save(nuevoToken);
    }

    //TODO validación del token...
}

