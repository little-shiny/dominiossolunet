package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenDominioRepository extends JpaRepository<TokenDominio, Integer> {

    Optional<TokenDominio> findByDominio(Dominio dominio);

    List<TokenDominio> findByTokenCliente(TokenCliente tokenCliente);

    Optional<TokenDominio> findFirstByDominioOrderByIdDesc(Dominio dominio);
}