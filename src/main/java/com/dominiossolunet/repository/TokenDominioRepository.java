package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenDominioRepository extends JpaRepository<TokenDominio, Integer> {
    Optional<TokenDominio> findByDominio(Dominio dominio);

    Optional<TokenDominio> findByEstadoAvisoRenovacion(EstadoAvisoRenovacion estadoAvisoRenovacion);

    List<TokenDominio> findByTokenCliente(TokenCliente tokenCliente);
}