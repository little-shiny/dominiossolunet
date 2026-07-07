package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.EstadoFacturacion;
import com.dominiossolunet.model.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {

    /**
     * Query que busca Facturación de un determinado dominio
     */
    Optional<Facturacion> findByDominio(Dominio dominio);

    /**
     * Query que busca todo lo que haya en función del estado de facturación
     */
    List<Facturacion> findByEstadoFacturacion(EstadoFacturacion estado);
}
