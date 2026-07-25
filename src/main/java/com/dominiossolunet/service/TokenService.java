package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Métodos a implementar: generar un nuevo token, validarlo(que existe, que no ha caducado, que no está usado
 *
 */
@Service
public class TokenService {

    private final TokenClienteRepository tokenClienteRepository; //final porque va en el constructor y es unico
    private final TokenDominioRepository tokenDominioRepository;

    @Value("${token.dias-expiracion}")
    private int diasExpiracionToken;

    //Constructor
    public TokenService(TokenClienteRepository tokenClienteRepository, TokenDominioRepository tokenDominioRepository) {
        this.tokenClienteRepository = tokenClienteRepository;
        this.tokenDominioRepository = tokenDominioRepository;
    }

    public TokenCliente generarToken(Dominio dominio){
        TokenCliente nuevoToken = new TokenCliente();

        nuevoToken.setFechaCreacion(LocalDateTime.now());
        nuevoToken.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracionToken));
        nuevoToken.setToken(UUID.randomUUID().toString());
        nuevoToken.setUsado(false);

        return tokenClienteRepository.save(nuevoToken);
    }

    public ResultadoValidacionRec validarToken(String token){

        Optional<TokenCliente> resultado = tokenClienteRepository.findByToken(token);
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

    // Reemplazable por el record en futuro
    @Transactional
    public void marcarComoUsado(String token){

        TokenCliente tokenCliente = tokenClienteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token no encontrado: " + token));

        tokenCliente.setUsado(true);
        // No necesitamos save (dirty checking)
    }

    /**
     * Devuelve una lista de TokenDominios mediante los id_dominio que se introducen por parámetro.
     * Acepta empty
     * @param idsDominios
     * @return emptyList si es vacío, Lista de TokenDominios
     */
    public List<TokenDominio> obtieneTokenDominiosPorIdDominio(List<Integer> idsDominios){

        if(idsDominios == null || idsDominios.isEmpty()){
            return Collections.emptyList();
        }

        return tokenDominioRepository.findByDominio_IdIn(idsDominios);
    }

    @Transactional
    public void marcarEstadoRenovacion(TokenDominio tokenDominio, EstadoAvisoRenovacion estado){

        TokenDominio tokenDominioResultado = tokenDominioRepository.findById(tokenDominio.getId())
                .orElseThrow(() -> new IllegalArgumentException("Token no encontrado con la id : " + tokenDominio.getId()));

        tokenDominioResultado.setEstadoAvisoRenovacion(estado);
    }
}

