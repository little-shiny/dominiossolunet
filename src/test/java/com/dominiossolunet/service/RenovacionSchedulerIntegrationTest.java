package com.dominiossolunet.scheduler;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RenovacionSchedulerIntegrationTest {

    @Autowired
    private RenovacionScheduler scheduler;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @BeforeEach
    void limpiarDatos() {
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void ejecutarRenovaciones_debeEjecutarElProcesoCompleto() {

        // ARRANGE

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@cliente.com");

        cliente = clienteRepository.saveAndFlush(cliente);

        Dominio dominio = new Dominio();
        dominio.setCliente(cliente);
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(20)
        );
        dominio.setRegistrador(Registrador.DOMITECA);

        dominio = dominioRepository.saveAndFlush(dominio);

        // ACT

        scheduler.ejecutarRenovaciones();

        // ASSERT

        Dominio dominioActualizado =
                dominioRepository
                        .findById(dominio.getId())
                        .orElseThrow();

        assertThat(dominioActualizado.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);
    }
}