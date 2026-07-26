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

    public TokenCliente generarToken(Dominio dominio) {
        TokenCliente nuevoToken = new TokenCliente();

        nuevoToken.setFechaCreacion(LocalDateTime.now());
        nuevoToken.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracionToken));
        nuevoToken.setToken(UUID.randomUUID().toString());
        nuevoToken.setUsado(false);

        return tokenClienteRepository.save(nuevoToken);
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

        TokenCliente tokenCliente = tokenClienteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token no encontrado: " + token));

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
    public void marcaEstadoRenovacionPorListaIdDominio(List<Integer> idsDominiosMarcadosCliente,
                                                       ResultadoValidacionRec resultadoValidacion) {

        // Se usa un Set que se crea a partir de la lista que obtenemos al enviar el formulario
        HashSet<Integer> idsMarcados = new HashSet<>(idsDominiosMarcadosCliente);

        // obtener la lista de TODOS los TokenDominio asociada a ese TokenCliente (No solo los marcados)
        List<TokenDominio> tokenDominioListaCompleta =
                tokenDominioRepository.findByTokenCliente(resultadoValidacion.token());

        // Para cada coincidencia entre el set y la lista se establece el estado
        for (TokenDominio td : tokenDominioListaCompleta) {

            EstadoAvisoRenovacion estado = (idsMarcados.contains(td.getDominio().getId())) ?
            EstadoAvisoRenovacion.CONFIRMADO : // En caso de que esté en ambas listas es porque el cliente lo ha
            // marcado
            EstadoAvisoRenovacion.RECHAZADO;// En caso contrario no se ha marcado y por tanto se establece como
            // RECHAZADO
            td.setEstadoAvisoRenovacion(estado); // Se establece el nuevo estado en la bd
        }

        resultadoValidacion.token().setUsado(true);

    }
}

