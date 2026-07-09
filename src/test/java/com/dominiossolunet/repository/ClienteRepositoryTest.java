package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class ClienteRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void guardarYRecuperarCliente(){
        // Preparacion de los datos de la bd
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        Cliente clienteGuardado = entityManager.persistAndFlush(cliente);
        //Aquí se comprueba que el entity manager ha introducido correctamente el cliente en h2 y ha generaqdo su id
        // correctamente
        assertThat(clienteGuardado.getId()).isNotZero();

        Optional<Cliente> clienteRecuperado = clienteRepository.findById(clienteGuardado.getId());

        //Assertions
        //Primero se comprueba el optional para ver que no esté vacío
        assertThat(clienteRecuperado.isPresent()).isTrue();
        //En el caso de que sea presente se puede buyscar con get el contenido:
        Cliente cliente1 = clienteRecuperado.get();
        assertThat(cliente1.getNombre()).isEqualTo("Ana");
        assertThat(cliente1.getEmail()).isEqualTo("ana@ana.com");









    }



}
