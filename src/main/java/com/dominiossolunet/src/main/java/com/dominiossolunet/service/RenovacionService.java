package com.dominiossolunet.src.main.java.com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenRenovacion;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.TokenRenovacionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * El servicio debe comprobar enb la bd los dominios con fecha de expiración en 30, 15, 5, 1
 */

@Service
public class RenovacionService {
    private final DominioRepository dominioRepository; // Es final porque va en el constructor
    private final TokenRenovacionRepository tokenRenovacionRepository;  //idem

    @Value("${renovacion.umbrales}")
    private List<Integer> umbrales;

    @Value("${token.dias-expiracion}")
    private int diasExpiracionToken;

    private int umbralMaximo; // Se asignará en el @Postconstruct

    //Constructor
    public RenovacionService(DominioRepository dominioRepository, TokenRenovacionRepository tokenRenovacionRepository) {
        this.dominioRepository = dominioRepository;
        this.tokenRenovacionRepository = tokenRenovacionRepository;
    }

    // Ciclo de vida de Spring: tras el constructor se inyectan @Value y @Autowired. Después llama a los metodos
    // anotados con @postconstruct y luego, el resto de la aplicacion. Esta anotacion se usa para inicializar
    // variables desde valores inyectados

    @PostConstruct
    private void calcularUmbralMaximo() {
        umbralMaximo = Collections.max(umbrales);
    }


    /**
     * Método que procesa los envíos de los avisos de expiración de los dominios
     * Filtra por los dominios que estan en el umbral de expiración y realiza el envío de correos a dichos clientes.
     * se utliza @Transacitonal en lugar de .save() para que en lugar de realizar una transacción por cada dominio se
     * haga únicamente una, así como evitar que si hay un fallo en mitad del proceso se quede "a medias"
     */
    @Transactional
    public void procesarAvisos() {
        // Estados que debemos filtrar para enviar el correo del aviso
        List<Estado> estadosValidos = List.of(Estado.ACTIVO, Estado.AVISO_ENVIADO);

        // Se obtiene la lista ejecutando la query
        List<Dominio> candidatos = dominioRepository.findByEstadoInAndFechaExpiracionBefore(estadosValidos, LocalDate.now().plusDays(umbralMaximo));

        for (Dominio dominio : candidatos) {
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), dominio.getFechaExpiracion());

            // Si existe un fallo en el cron, umbralquetoca sigue siendo el mayor porque es >= que dicho umbral y se
            // manda una sola vez.
            Integer umbralQueToca = null;
            for (int umbral : umbrales) {
                if (umbral >= diasRestantes) {
                    umbralQueToca = umbral;
                }
            }

            if (umbralQueToca != null && !umbralQueToca.equals(dominio.getUltimoUmbralAvisado())) {
                // actualizar el dominio con el nuevo estado y otro para el nuevo umbral aviso
                dominio.setEstado(Estado.AVISO_ENVIADO);
                dominio.setUltimoUmbralAvisado(umbralQueToca);
                dominio.setUltimoAviso(LocalDate.now()); // debug


            }
        }
    }

    //TODO: Genración de los tokens en su propio servicio, asignación temporal:
    private TokenRenovacion generarToken(Dominio dominio) {

        TokenRenovacion nuevoToken = new TokenRenovacion();
        nuevoToken.setFechaCreacion(LocalDateTime.now());
        nuevoToken.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracionToken));
        nuevoToken.setDominio(dominio);
        nuevoToken.setToken(UUID.randomUUID().toString());
        nuevoToken.setUsado(false);

        return tokenRenovacionRepository.save(nuevoToken);
    }

    //TODO:EmailService


}
