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

    @Test
    void guardarYBuscarClientePorEmailExiste(){
        //Inserción de datos de prueba
        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Pepe");
        cliente1.setEmail("pepe@pepe.com");
        Cliente Cliente1Guardado = entityManager.persistAndFlush(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Maria");
        cliente2.setEmail("maria@maria.com");
        Cliente Cliente2Guardado = entityManager.persistAndFlush(cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setNombre("sara");
        cliente3.setEmail("sara@sara.com");
        Cliente Cliente3Guardado = entityManager.persistAndFlush(cliente3);

        // act
        Optional<Cliente> clienteRecuperado = clienteRepository.findByEmail("maria@maria.com");

        //assert
        assertThat(clienteRecuperado).isPresent();
        assertThat(clienteRecuperado.get().getNombre()).isEqualTo("Maria");
        assertThat(clienteRecuperado.get().getEmail()).isEqualTo("maria@maria.com");
    }

    @Test
    void guardarYBuscarClientePorEmailENoExiste(){
        //Inserción de datos de prueba
        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Pepe");
        cliente1.setEmail("pepe@pepe.com");
        Cliente Cliente1Guardado = entityManager.persistAndFlush(cliente1);
        // act
        Optional<Cliente> clienteRecuperado = clienteRepository.findByEmail("maria@maria.com");

        //assert
        assertThat(clienteRecuperado).isNotPresent();
    }



    }
