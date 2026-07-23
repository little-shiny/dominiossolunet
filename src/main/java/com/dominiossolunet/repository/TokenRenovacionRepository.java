package com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRenovacionRepository extends JpaRepository<TokenCliente, Integer> {
    Optional<TokenCliente> findByToken(String token);

    /**
     * Busca tokens NO USADOS de un determinado dominio
     */
    Optional<TokenCliente> findByDominioAndUsado(Dominio dominio, boolean usado);
}
