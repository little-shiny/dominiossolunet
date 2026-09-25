package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistorialDominioRepository
        extends JpaRepository<HistorialDominio, Integer> {

    List<HistorialDominio> findByDominioOrderByFechaDesc(Dominio dominio);
}