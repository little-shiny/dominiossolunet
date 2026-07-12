package com.dominiossolunet.repository;

import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {

    /**
     * Query que busca Facturación de un determinado dominio
     */
    Optional<Facturacion> findByDominio_NombreDominio(String nombreDominio);


    /**
     * Query que busca las entradas de la tabla facturacion dependiendo del estado
     */
    List<Facturacion> findByEstadoFacturacion(EstadoFacturacion estado);
}
