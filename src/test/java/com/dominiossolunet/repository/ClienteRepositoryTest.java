package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class ClienteRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    // Preparacion de los datos de la bd
    Cliente cliente = new Cliente();
    cliente.setNombre("Ana");
}
