package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturacionRepository
        extends JpaRepository<Facturacion, Integer> {

    Optional<Facturacion> findByDominio(Dominio dominio);

    Optional<Facturacion> findByDominio_NombreDominio(String nombreDominio);

    List<Facturacion> findByEstadoFacturacion(
            EstadoFacturacion estado
    );
}