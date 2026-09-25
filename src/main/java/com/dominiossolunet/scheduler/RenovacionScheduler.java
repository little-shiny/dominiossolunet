package com.dominiossolunet.scheduler;

import com.dominiossolunet.service.RenovacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RenovacionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RenovacionScheduler.class);

    private final RenovacionService renovacionService;

    public RenovacionScheduler(RenovacionService renovacionService){
        this.renovacionService = renovacionService;
    }

    @Scheduled(cron = "${renovacion.scheduler.cron}")
    public void ejecutarRenovaciones(){

        logger.info("Inicio del scheduler de las renovaciones");
        renovacionService.procesarAvisos();

        logger.info("Fin del scheduler de renovaciones");
    }
}
