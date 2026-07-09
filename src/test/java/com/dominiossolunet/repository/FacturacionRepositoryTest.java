package com.dominiossolunet.repository;

import com.dominiossolunet.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class FacturacionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @Autowired ClienteRepository clienteRepository;

    @Autowired DominioRepository dominioRepository;

    @Test
    void guardarYRecuperarFacturacionPorDominio(){

        // Creación del cliente asociado al dominio
        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Ana");
        cliente1.setEmail("ana@ana.com");
        Cliente clienteGuardado = entityManager.persistAndFlush(cliente1);
        assertThat(clienteGuardado.getId()).isNotZero();

        // Creación del dominio
        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setCliente(cliente1);
        Dominio dominioGuardado = entityManager.persistAndFlush(dominio);
        assertThat(dominioGuardado.getId()).isNotZero();

        //Creación de fila en facturación
        Facturacion facturacion = new Facturacion();
        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(EstadoFacturacion.PENDIENTE_FACTURAR);
        facturacion.setFechaUltimaFactura(LocalDate.now());
        Facturacion facturaGuardada = entityManager.persistAndFlush(facturacion);

        assertThat(facturaGuardada.getId()).isNotZero();

        // Assertions
        //Ejecutamos la query en un optional
        Optional<Facturacion> facturaRecuperada = facturacionRepository.findByDominio_NombreDominio("ana.com");

        //Comprobamos que no esta vacio
        assertThat(facturaRecuperada.isPresent()).isTrue();

        //Comprobamos que efectivamente coincide con el nombre del dominio y su cliente asociado
        assertThat(facturaRecuperada.get().getDominio().getNombreDominio()).isEqualTo("ana.com");
        assertThat(facturaRecuperada.get().getDominio().getEstado()).isEqualTo(Estado.ACTIVO);
        assertThat(facturaRecuperada.get().getDominio().getCliente().getNombre()).isEqualTo("Ana");





    }
}
