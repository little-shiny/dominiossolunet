package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.repository.DominioRepository;
import jakarta.annotation.PostConstruct;
import lombok.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El servicio debe comprobar enb la bd los dominios con fecha de expiración en  30, 15, 5, 1
 */

@Service
public class RenovacionService {
    private final DominioRepository dominioRepository; // Es final porque va en el constructor

    @Value("${renovacion.umbrales}")
    private List<Integer> umbrales;

    private int umbralMaximo; // Se asignará en el @Postconstruct

    //Constructor
    public RenovacionService(DominioRepository dominioRepository){
        this.dominioRepository = dominioRepository;
    }

    // Ciclo de vida de Spring: tras el constructor se inyectan @Value y @Autowired. Después llama a los metodos
    // anotados con @postconstruct y luego, el resto de la aplicacion. Esta anotacion se usa para inicializar
    // variables desde valores inyectados

    @PostConstruct
    private void calcularUmbralMaximo(){
        umbralMaximo = Collections.max(umbrales);
    }

    public void procesarAvisos(){
        // Estados que debemos filtrar para enviar el correo del aviso
        List<Estado> estadosValidos = List.of(Estado.ACTIVO, Estado.AVISO_ENVIADO);

        // Se obtiene la lista ejecutando la query
        List<Dominio> candidatos = dominioRepository.findByEstadoInAndFechaExpiracionBefore(estadosValidos,
                LocalDate.now().plusDays(umbralMaximo));

        for (Dominio dominio : candidatos){
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), dominio.getFechaExpiracion());

            // Si existe un fallo en el cron, umbralquetoca sigue siendo el mayor porque es >= que dicho umbral y se
            // manda una sola vez.
            Integer umbralQueToca = null;
            for (int umbral : umbrales){
                if (umbral >= diasRestantes){
                    umbralQueToca = umbral;
                }
            }

            if (umbralQueToca != null && umbralQueToca.equals(dominio.getUltimoUmbralAvisado())){
                // Generar token, enviar email, actualizar estado y ultimoumbralavisado
            }
        }
    }


}
