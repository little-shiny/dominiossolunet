package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de los tokens de renovación
 * y de las respuestas de los clientes a los avisos de renovación.
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final TokenClienteRepository tokenClienteRepository;
    private final TokenDominioRepository tokenDominioRepository;
    private final HistorialDominioRepository historialDominioRepository;

    @Value("${token.dias-expiracion}")
    private int diasExpiracionToken;

    public TokenService(TokenClienteRepository tokenClienteRepository, TokenDominioRepository tokenDominioRepository, HistorialDominioRepository historialDominioRepository) {

        this.tokenClienteRepository = tokenClienteRepository;
        this.tokenDominioRepository = tokenDominioRepository;
        this.historialDominioRepository = historialDominioRepository;
    }

    /**
     * Crea el registro de historial correspondiente
     * a la respuesta del cliente.
     *
     * @param dominio        dominio sobre el que se ha respondido
     * @param ahora          fecha y hora de la respuesta
     * @param marcadoRenovar true si el cliente acepta renovar
     * @return registro de historial
     */
    @Nonnull
    private static HistorialDominio crearHistorialRespuestaCliente(Dominio dominio, LocalDateTime ahora, boolean marcadoRenovar) {

        HistorialDominio historial = new HistorialDominio();

        historial.setDominio(dominio);
        historial.setFecha(ahora);

        if (marcadoRenovar) {

            historial.setTipoEvento(TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION);

            historial.setDetalle("El cliente ha aceptado la renovación del dominio");

        } else {

            historial.setTipoEvento(TipoEventoDominio.CLIENTE_RECHAZA_RENOVACION);

            historial.setDetalle("El cliente ha rechazado la renovación del dominio");
        }

        return historial;
    }

    /**
     * Genera un token para un cliente y crea la relación
     * entre el token y cada uno de sus dominios.
     *
     * @param cliente  cliente al que pertenece el aviso
     * @param dominios dominios incluidos en el aviso
     * @return token generado
     */
    @Transactional
    public TokenCliente generarToken(Cliente cliente, List<Dominio> dominios) {

        LocalDateTime ahora = LocalDateTime.now();

        TokenCliente tokenCliente = new TokenCliente();

        tokenCliente.setFechaCreacion(ahora);
        tokenCliente.setFechaExpiracion(ahora.plusDays(diasExpiracionToken));
        tokenCliente.setToken(UUID.randomUUID().toString());
        tokenCliente.setUsado(false);
        tokenCliente.setCliente(cliente);

        tokenCliente = tokenClienteRepository.save(tokenCliente);

        logger.info("TokenCliente guardado correctamente para el cliente {}", cliente.getNombre());

        for (Dominio dominio : dominios) {

            TokenDominio tokenDominio = new TokenDominio();

            tokenDominio.setTokenCliente(tokenCliente);
            tokenDominio.setDominio(dominio);
            tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.PENDIENTE);

            tokenDominioRepository.save(tokenDominio);
        }

        logger.info("Añadidos {} TokenDominio al cliente {}", dominios.size(), cliente.getNombre());

        return tokenCliente;
    }

    /**
     * Valida un token comprobando:
     * - que exista;
     * - que no haya sido utilizado;
     * - que no haya expirado.
     *
     * @param token valor del token
     * @return resultado de la validación
     */
    public ResultadoValidacionRec validarToken(String token) {

        Optional<TokenCliente> resultado = tokenClienteRepository.findByToken(token);

        if (resultado.isEmpty()) {

            return new ResultadoValidacionRec(ResultadoValidacion.NO_ENCONTRADO, null);
        }

        TokenCliente tokenEncontrado = resultado.get();

        if (tokenEncontrado.isUsado()) {

            return new ResultadoValidacionRec(ResultadoValidacion.USADO, tokenEncontrado);
        }

        if (tokenEncontrado.getFechaExpiracion().isBefore(LocalDateTime.now())) {

            return new ResultadoValidacionRec(ResultadoValidacion.EXPIRADO, tokenEncontrado);
        }

        return new ResultadoValidacionRec(ResultadoValidacion.VALIDO, tokenEncontrado);
    }

    /**
     * Marca un token como utilizado.
     * <p>
     * Al estar dentro de una transacción, no es necesario
     * realizar explícitamente save() después de modificar
     * la entidad.
     *
     * @param token valor del token
     */
    @Transactional
    public void marcarComoUsado(String token) {

        TokenCliente tokenCliente = tokenClienteRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Token no encontrado: " + token));

        tokenCliente.setUsado(true);

        logger.info("Token marcado como usado para el cliente {}", tokenCliente.getCliente().getNombre());
    }

    /**
     * Obtiene todos los TokenDominio asociados a un TokenCliente.
     *
     * @param token token del cliente
     * @return lista de relaciones token-dominio
     */
    public List<TokenDominio> obtenerTokenDominioPorTokenCliente(TokenCliente token) {

        return tokenDominioRepository.findByTokenCliente(token);
    }

    /**
     * Procesa la respuesta del cliente al formulario de renovación.
     * <p>
     * Para cada dominio:
     * <p>
     * - Actualiza el estado de la respuesta del TokenDominio.
     * - Registra la fecha de interacción del cliente.
     * - Actualiza el estado de renovación del dominio.
     * - Registra el evento correspondiente en el historial.
     * <p>
     * Finalmente, marca el TokenCliente como utilizado.
     * <p>
     * Es importante distinguir:
     * <p>
     * CLIENTE_ACEPTA_RENOVACION
     * ↓
     * PENDIENTE_RENOVACION
     * <p>
     * Esto NO significa todavía que el dominio haya sido
     * renovado en el registrador.
     * <p>
     * La renovación real se registrará posteriormente
     * desde el panel de gestión.
     *
     * @param token               token del cliente
     * @param idsDominiosMarcados IDs de los dominios que el cliente acepta renovar
     */
    @Transactional
    public void procesarConfirmacion(TokenCliente token, List<Integer> idsDominiosMarcados) {

        HashSet<Integer> idsMarcados = new HashSet<>(idsDominiosMarcados);

        List<TokenDominio> tokenDominioList = obtenerTokenDominioPorTokenCliente(token);

        LocalDateTime ahora = LocalDateTime.now();

        logger.info("Procesando respuesta del cliente {}. Dominios marcados: {}", token.getCliente().getNombre(), idsDominiosMarcados.size());

        for (TokenDominio tokenDominio : tokenDominioList) {

            Dominio dominio = tokenDominio.getDominio();

            boolean marcadoRenovar = idsMarcados.contains(dominio.getId());

            /*
             * Guardamos la fecha en la que el cliente
             * respondió sobre este dominio.
             */
            tokenDominio.setFechaInteraccionCliente(ahora);

            /*
             * Actualizamos el estado de la respuesta
             * asociada al aviso.
             */
            if (marcadoRenovar) {

                tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.CONFIRMADO);

                /*
                 * El cliente ha aceptado renovar.
                 *
                 * Todavía NO significa que el dominio
                 * haya sido renovado en el registrador.
                 */
                dominio.setEstadoRenovacion(EstadoRenovacion.PENDIENTE_RENOVACION);

                logger.info("El cliente ha aceptado renovar el dominio {}", dominio.getNombreDominio());

            } else {

                tokenDominio.setEstadoAvisoRenovacion(EstadoAvisoRenovacion.RECHAZADO);

                /*
                 * El cliente ha rechazado la renovación.
                 */
                dominio.setEstadoRenovacion(EstadoRenovacion.RECHAZADO);

                logger.info("El cliente ha rechazado renovar el dominio {}", dominio.getNombreDominio());
            }

            /*
             * Registramos el evento permanente en el historial.
             */
            HistorialDominio historial = crearHistorialRespuestaCliente(dominio, ahora, marcadoRenovar);

            historialDominioRepository.save(historial);
        }

        /*
         * El cliente ya ha terminado de responder
         * al formulario.
         */
        marcarComoUsado(token.getToken());
    }
}
