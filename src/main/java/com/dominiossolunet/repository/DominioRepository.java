package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DominioRepository extends JpaRepository<Dominio, Integer> {

    /**
     * Método que genera una lista con los dominios según el estado elegido y la fecha de expiración
     */
    List<Dominio> findByEstadoInAndFechaExpiracionLessThanEqual(List<Estado> estados, LocalDate fecha);

    /**
     * query mas avanzada que incluye una lista de estados
     */
    List<Dominio> findByEstadoInAndFechaExpiracionBefore(List<Estado> estados, LocalDate fecha);

}
