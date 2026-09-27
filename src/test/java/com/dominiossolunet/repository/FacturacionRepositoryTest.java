package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.Registrador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class FacturacionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FacturacionRepository facturacionRepository;

    @Test
    void guardarYRecuperarFacturacionPorDominio() {

        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        Cliente clienteGuardado =
                entityManager.persistAndFlush(cliente);

        assertThat(clienteGuardado.getId()).isNotZero();

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setCliente(cliente);
        dominio.setRegistrador(Registrador.DOMITECA);

        Dominio dominioGuardado =
                entityManager.persistAndFlush(dominio);

        assertThat(dominioGuardado.getId()).isNotZero();

        Facturacion facturacion = new Facturacion();
        facturacion.setDominio(dominio);
        facturacion.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );
        facturacion.setFechaUltimaFactura(LocalDate.now());

        Facturacion facturaGuardada =
                entityManager.persistAndFlush(facturacion);

        assertThat(facturaGuardada.getId()).isNotZero();

        // WHEN
        Optional<Facturacion> facturaRecuperada =
                facturacionRepository.findByDominio_NombreDominio(
                        "ana.com"
                );

        // THEN
        assertThat(facturaRecuperada.isPresent()).isTrue();

        assertThat(
                facturaRecuperada.get()
                        .getDominio()
                        .getNombreDominio()
        ).isEqualTo("ana.com");

        assertThat(
                facturaRecuperada.get()
                        .getDominio()
                        .getEstado()
        ).isEqualTo(Estado.ACTIVO);

        assertThat(
                facturaRecuperada.get()
                        .getDominio()
                        .getCliente()
                        .getNombre()
        ).isEqualTo("Ana");

        assertThat(
                facturaRecuperada.get()
                        .getEstadoFacturacion()
        ).isEqualTo(EstadoFacturacion.PENDIENTE_FACTURAR);
    }

    @Test
    void guardarYRecuperarFacturacionPorEstado() {

        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        Cliente clienteGuardado =
                entityManager.persistAndFlush(cliente);

        assertThat(clienteGuardado.getId()).isNotZero();

        Dominio dominio1 = new Dominio();
        dominio1.setNombreDominio("ana.com");
        dominio1.setEstado(Estado.ACTIVO);
        dominio1.setCliente(cliente);
        dominio1.setRegistrador(Registrador.DOMITECA);

        Dominio dominioGuardado1 =
                entityManager.persistAndFlush(dominio1);

        assertThat(dominioGuardado1.getId()).isNotZero();

        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("anaproyectos.com");
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setCliente(cliente);
        dominio2.setRegistrador(Registrador.DOMITECA);

        Dominio dominioGuardado2 =
                entityManager.persistAndFlush(dominio2);

        assertThat(dominioGuardado2.getId()).isNotZero();

        Facturacion facturacion1 = new Facturacion();
        facturacion1.setDominio(dominio1);
        facturacion1.setEstadoFacturacion(
                EstadoFacturacion.PENDIENTE_FACTURAR
        );
        facturacion1.setFechaUltimaFactura(LocalDate.now());

        Facturacion facturaGuardada1 =
                entityManager.persistAndFlush(facturacion1);

        assertThat(facturaGuardada1.getId()).isNotZero();

        Facturacion facturacion2 = new Facturacion();
        facturacion2.setDominio(dominio2);
        facturacion2.setEstadoFacturacion(
                EstadoFacturacion.FACTURADO
        );
        facturacion2.setFechaUltimaFactura(LocalDate.now());

        Facturacion facturaGuardada2 =
                entityManager.persistAndFlush(facturacion2);

        assertThat(facturaGuardada2.getId()).isNotZero();

        // WHEN
        List<Facturacion> facturasRecuperadas =
                facturacionRepository.findByEstadoFacturacion(
                        EstadoFacturacion.FACTURADO
                );

        // THEN
        assertThat(facturasRecuperadas.size()).isEqualTo(1);

        assertThat(
                facturasRecuperadas.getFirst()
                        .getDominio()
                        .getNombreDominio()
        ).isEqualTo("anaproyectos.com");

        assertThat(
                facturasRecuperadas.getFirst()
                        .getEstadoFacturacion()
        ).isEqualTo(EstadoFacturacion.FACTURADO);
    }
}