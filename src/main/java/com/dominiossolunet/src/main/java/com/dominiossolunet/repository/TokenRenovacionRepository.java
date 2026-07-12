package com.dominiossolunet.src.main.java.com.dominiossolunet.repository;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenRenovacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRenovacionRepository extends JpaRepository<TokenRenovacion, Integer> {
    Optional<TokenRenovacion> findByToken(String token);

    /**
     * Busca tokens NO USADOS de un determinado dominio
     */
    Optional<TokenRenovacion> findByDominioAndUsado(Dominio dominio, boolean usado);
}
