package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenDominioRepository extends JpaRepository<TokenDominio, Integer> {
    Optional<TokenDominio> findByDominio(Dominio dominio);

    Optional<TokenDominio> findByEstadoAvisoRenovacion(EstadoAvisoRenovacion estadoAvisoRenovacion);

    /**
     * Query que proporciona una lista de TokenDominio a partir de una lista de id_dominio
     * @param ids
     * @return lista TokenDominio
     */
}