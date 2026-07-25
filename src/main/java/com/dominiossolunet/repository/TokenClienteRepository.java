package com.dominiossolunet.repository;

import com.dominiossolunet.model.TokenCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenClienteRepository extends JpaRepository<TokenCliente, Integer> {
    Optional<TokenCliente> findByToken(String token);
}
