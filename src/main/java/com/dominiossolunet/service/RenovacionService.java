package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.utils.RenovacionUrlBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * El servicio debe comprobar enb la bd los dominios con fecha de expiración en 30, 15, 5, 1
 */

@Service
public class RenovacionService {
    private static final Logger logger = LoggerFactory.getLogger(RenovacionService.class);
    private final DominioRepository dominioRepository; // Es final porque va en el constructor
    private final TokenService tokenService;  //idem
    private final EmailService emailService;
    private final RenovacionUrlBuilder renovacionUrlBuilder;
    @Value("${renovacion.umbrales}")
    private List<Integer> umbrales;
    private int umbralMaximo;

    //Constructor
    public RenovacionService(DominioRepository dominioRepository, TokenService tokenService, EmailService emailService, RenovacionUrlBuilder renovacionUrlBuilder) {
        this.dominioRepository = dominioRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.renovacionUrlBuilder = renovacionUrlBuilder;
    }

    /**
     * Calcula el mayor umbral configurado una vez que Spring * ha inyectado la propiedad renovacion.umbrales.
     */
    @PostConstruct
    private void calcularUmbralMaximo() {
        if (umbrales == null || umbrales.isEmpty()) {
            throw new IllegalStateException("Debe existir al menos un umbral de renovación configurado");
        }
        umbralMaximo = Collections.max(umbrales);
    }

    /**
     * Procesa los avisos de renovación de los dominios próximos a expirar.
     * <p>
     * El proceso:
     * 1. Busca los dominios dentro del mayor umbral configurado.
     * 2. Ignora los dominios ya expirados.
     * 3. Determina el umbral que corresponde a cada dominio.
     * 4. Evita volver a procesar un dominio para el mismo umbral.
     * 5. Actualiza el estado y la información del último aviso.
     * 6. Agrupa los dominios por cliente.
     * 7. Genera un único TokenCliente por cliente.
     * <p>
     * El envío del correo se realizará posteriormente mediante EmailService.
     *
     */

    @Transactional
    public void procesarAvisos() {
        logger.info("Inicio del proceso de avisos");

        List<ErrorEnvioEmail> erroresEnvio = new ArrayList<>();

        LocalDate hoy = LocalDate.now();

        List<Estado> estadosValidos = List.of(Estado.ACTIVO, Estado.AVISO_ENVIADO);

        List<Dominio> candidatos = dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(estadosValidos, hoy.plusDays(umbralMaximo));

        Map<Cliente, List<Dominio>> dominiosPorCliente = candidatos.stream()
                // No se procesan dominios que ya han expirado
                .filter(dominio -> !dominio.getFechaExpiracion().isBefore(hoy))

                //Solo se procesan los dominios que necesitan un nuevo aviso
                .filter(dominio -> necesitaAviso(dominio, hoy))

                // Se agrupan los dominios que necesitan un aviso por cliente
                .collect(Collectors.groupingBy(Dominio::getCliente));

        logger.info("Clientes con dominios por avisar: {}", dominiosPorCliente.size());

        /* Se genera un único token por cliente
         * De esta forma si un cliente tiene varios dominios próximos a expirar recibirá un aviso único con todos ellos
         */
        for (Map.Entry<Cliente, List<Dominio>> entrada : dominiosPorCliente.entrySet()) {

            Cliente cliente = entrada.getKey();
            List<Dominio> dominios = entrada.getValue();

            logger.info("Procesando cliente {} con {} dominios pendientes", cliente.getNombre(), dominios.size());

            TokenCliente token = tokenService.generarToken(cliente, dominios);

            logger.info("Token generado para el cliente {}", cliente.getNombre());

            // Creación de la url de la renovación
            String urlRenovacion = renovacionUrlBuilder.construirUrlConfirmacion(token.getToken());

            ResultadoEnvioEmail resultado = emailService.enviarAvisoRenovacion(cliente, dominios, urlRenovacion);

            if (resultado.isEnviado()) {
                marcarComoAvisado(dominios, hoy);
            } else {
                erroresEnvio.add(new ErrorEnvioEmail(cliente, dominios, resultado.getMensajeError()));
            }
        }

        ResultadoEnvioEmail resultadoInforme = emailService.enviarInformeRenovacion(erroresEnvio);

        if (resultadoInforme.isEnviado()) {
            logger.info("Informe de renovación enviado correctamente al administrador");
        } else {
            logger.error("No se pudo enviar el informe al administrador - {}", resultadoInforme.getMensajeError());
        }
    }

    /**
     * Determina si un dominio necesita recibir un nuevo aviso
     * <p>
     * Un dominio necesita un aviso cuando:
     * - Se encuentra dentro de alguno de los umbrales configurados.
     * - El umbral correspondiente es diferente al ultimo umbral que ya se notificó
     */
    private boolean necesitaAviso(Dominio dominio, LocalDate hoy) {
        Integer umbral = determinarUmbral(dominio, hoy);

        if (umbral == null) {
            return false;
        }

        return !umbral.equals(dominio.getUltimoUmbralAvisado());
    }

    /**
     * Determina el umbral correspondiente a los días restantes
     * <p>
     * Se selecciona el menor umbral que sea mayor o igual a los días restantes a la expiración
     * <p>
     * Ejemplo con umbrales [30, 15, 5, 1]
     * 27 días -> 30
     * 14 días -> 15
     * 4 días -> 5 ...
     */
    private Integer determinarUmbral(Dominio dominio, LocalDate hoy) {

        long diasRestantes = ChronoUnit.DAYS.between(hoy, dominio.getFechaExpiracion());

        return umbrales.stream().filter(umbral -> umbral >= diasRestantes).min(Integer::compareTo).orElse(null);
    }

    /**
     * Marcar como avisado un dominio
     * Recorre la lista de dominios por parametro , guarda su umbral, cambia el estado a aviso como enviado y guarda
     *
     */
    private void marcarComoAvisado(List<Dominio> dominios, LocalDate fechaActual) {
        for (Dominio dominio : dominios) {

            Integer umbral = determinarUmbral(dominio, fechaActual);

            dominio.setEstado(Estado.AVISO_ENVIADO);
            dominio.setUltimoUmbralAvisado(umbral);
            dominio.setUltimoAviso(fechaActual);

        }
    }
}
