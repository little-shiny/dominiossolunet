package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Estado;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class DominioRepositoryTest {
    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deberia_encontrar_dominios_por_estado_y_fecha_antes_de() {
        // crear y guardar un Cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Pepito");
        cliente.setEmail("pepito99@pepito.es");

        // crear un Dominio asociado a ese cliente
        Dominio dominio = new Dominio();
        dominio.setCliente(cliente);
        dominio.setEstado(Estado.ACTIVO);
        dominio.setFechaExpiracion(LocalDate.now().plusDays(10));
        dominio.setNombreDominio("pepitoysuscosas.com");
        dominioRepository.save(dominio);

        //llamar al método derivado que se quiere probar
        List<Dominio> resultado = dominioRepository.findByEstadoAndFechaExpiracionBefore(
                Estado.ACTIVO,
                LocalDate.now().plusDays(20)
        );
        assertEquals(1, resultado.size());
        assertEquals("pepitoysuscosas.com", resultado.get(0).getNombreDominio());
        assertEquals(Estado.ACTIVO, resultado.get(0).getEstado());
    }

}
