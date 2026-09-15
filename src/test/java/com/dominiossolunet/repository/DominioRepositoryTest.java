package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DominioRepositoryTest {

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deberia_encontrar_dominios_por_estado_y_fecha_menor_o_igual() {

        // Arrange

        Cliente cliente = new Cliente();
        cliente.setNombre("Pepito");
        cliente.setEmail("pepito99@pepito.es");
        entityManager.persistAndFlush(cliente);

        LocalDate fechaLimite = LocalDate.now().plusDays(30);

        // Dominio que expira antes de la fecha límite
        Dominio dominioAntes = new Dominio();
        dominioAntes.setCliente(cliente);
        dominioAntes.setEstado(Estado.ACTIVO);
        dominioAntes.setFechaExpiracion(LocalDate.now().plusDays(10));
        dominioAntes.setNombreDominio("pepitoysuscosas.com");
        dominioAntes.setRegistrador(Registrador.OTRO);
        entityManager.persistAndFlush(dominioAntes);

        // Dominio que expira exactamente en la fecha límite
        Dominio dominioExactamenteLimite = new Dominio();
        dominioExactamenteLimite.setCliente(cliente);
        dominioExactamenteLimite.setEstado(Estado.ACTIVO);
        dominioExactamenteLimite.setFechaExpiracion(fechaLimite);
        dominioExactamenteLimite.setNombreDominio("pepitolimite.com");
        dominioExactamenteLimite.setRegistrador(Registrador.OTRO);
        entityManager.persistAndFlush(dominioExactamenteLimite);

        // Dominio que expira después de la fecha límite
        Dominio dominioDespues = new Dominio();
        dominioDespues.setCliente(cliente);
        dominioDespues.setEstado(Estado.ACTIVO);
        dominioDespues.setFechaExpiracion(LocalDate.now().plusDays(40));
        dominioDespues.setNombreDominio("pepitodespues.com");
        dominioDespues.setRegistrador(Registrador.OTRO);
        entityManager.persistAndFlush(dominioDespues);

        // Act

        List<Dominio> resultado =
                dominioRepository.findByEstadoInAndFechaExpiracionLessThanEqual(
                        List.of(Estado.ACTIVO),
                        fechaLimite
                );

        // Assert

        assertEquals(2, resultado.size());

        assertTrue(
                resultado.stream()
                        .anyMatch(dominio ->
                                dominio.getNombreDominio()
                                        .equals("pepitoysuscosas.com"))
        );

        assertTrue(
                resultado.stream()
                        .anyMatch(dominio ->
                                dominio.getNombreDominio()
                                        .equals("pepitolimite.com"))
        );

        assertFalse(
                resultado.stream()
                        .anyMatch(dominio ->
                                dominio.getNombreDominio()
                                        .equals("pepitodespues.com"))
        );
    }
}