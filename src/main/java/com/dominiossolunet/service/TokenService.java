package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public TokenCliente generarToken(Cliente cliente, List<Dominio> dominios) {

        LocalDateTime ahora = LocalDateTime.now();

        TokenCliente tokenCliente = new TokenCliente();

        tokenCliente.setFechaCreacion(ahora);
        tokenCliente.setFechaExpiracion(ahora.plusDays(diasExpiracionToken));

        tokenCliente.setToken(UUID.randomUUID().toString());
        tokenCliente.setUsado(false);
        tokenCliente.setCliente(cliente);

        tokenCliente = tokenClienteRepository.save(tokenCliente);

        for (Dominio dominio : dominios) {
            TokenDominio tokenDominio = new TokenDominio();

            tokenDominio.setTokenCliente(tokenCliente);
            tokenDominio.setDominio(dominio);
            tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.PENDIENTE);

            tokenDominioRepository.save(tokenDominio);
        }
        return tokenCliente;
    }

    public ResultadoValidacionRec validarToken(String token) {

        Optional<TokenCliente> resultado = tokenClienteRepository.findByToken(token);
        TokenCliente tokenEncontrado;

        if (resultado.isPresent()) {
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
    public void marcarComoUsado(String token) {

        TokenCliente tokenCliente = tokenClienteRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Token no encontrado: " + token));

        tokenCliente.setUsado(true);
        // No necesitamos save (dirty checking)
    }

    public List<TokenDominio> obtenerTokenDominioPorTokenCliente(TokenCliente token) {
        return tokenDominioRepository.findByTokenCliente(token);
    }

    /**
     * Metodo que a partir de una lista de integers con las ids de los dominios marcados por el usuario marca cada
     * uno de los TokenDominio como tramite aceptado o rechazado según el cliente haya especificado
     */
    @Transactional
    public void marcaEstadoRenovacionPorListaIdDominio(
            List<Integer> idsDominiosMarcadosCliente,
            ResultadoValidacionRec resultadoValidacion) {

        TokenCliente tokenCliente =
                tokenClienteRepository.findByToken(
                        resultadoValidacion.token().getToken()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Token no encontrado: "
                                        + resultadoValidacion.token().getToken()
                        )
                );

        HashSet<Integer> idsMarcados =
                new HashSet<>(idsDominiosMarcadosCliente);

        List<TokenDominio> tokenDominioListaCompleta =
                tokenDominioRepository.findByTokenCliente(tokenCliente);

        for (TokenDominio td : tokenDominioListaCompleta) {

            if (idsMarcados.contains(td.getDominio().getId())) {
                td.setEstadoAvisoRenovacion(
                        EstadoAvisoRenovacion.CONFIRMADO
                );
            } else {
                td.setEstadoAvisoRenovacion(
                        EstadoAvisoRenovacion.RECHAZADO
                );
            }
        }

        tokenCliente.setUsado(true);
    }


    @Transactional
    public void procesarConfirmacion(TokenCliente token, List<Integer> idsDominiosMarcados) {

        HashSet<Integer> setTokens = new HashSet<>(idsDominiosMarcados);

        // obtener la lista de TokenDominio asociada a ese Tokencliente
        List<TokenDominio> tokenDominioList =
                obtenerTokenDominioPorTokenCliente(token);

        for (TokenDominio td : tokenDominioList) {
            EstadoAvisoRenovacion estado = (setTokens.contains(td.getDominio().getId())) ?
                    EstadoAvisoRenovacion.CONFIRMADO :
                    EstadoAvisoRenovacion.RECHAZADO;
            td.setEstadoAvisoRenovacion(estado);
        }

        marcarComoUsado(token.getToken());
    }
}

