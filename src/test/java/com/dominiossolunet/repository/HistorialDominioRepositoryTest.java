package com.dominiossolunet.repository;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class HistorialDominioRepositoryTest {

    @Autowired
    HistorialDominioRepository historialDominioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void guardarYRecuperarHistorialDominiosOrdenDesc() {

        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        Cliente clienteGuardado = entityManager.persistAndFlush(cliente);

        assertThat(clienteGuardado.getId()).isNotZero();

        // Dominio
        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setCliente(cliente);
        dominio.setRegistrador(Registrador.DOMITECA);

        Dominio dominioGuardado = entityManager.persistAndFlush(dominio);

        assertThat(dominioGuardado.getId()).isNotZero();

        // Primer evento
        HistorialDominio historialDominio = new HistorialDominio();
        historialDominio.setDominio(dominio);
        historialDominio.setTipoEvento(
                TipoEventoDominio.AVISO_RENOVACION_ENVIADO
        );
        historialDominio.setFecha(
                LocalDateTime.now().minusDays(3)
        );
        historialDominio.setDetalle("Comentario de ejemplo");

        // Segundo evento
        HistorialDominio historialDominio1 = new HistorialDominio();
        historialDominio1.setDominio(dominio);
        historialDominio1.setTipoEvento(
                TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION
        );
        historialDominio1.setFecha(
                LocalDateTime.now()
        );
        historialDominio1.setDetalle("Comentario de ejemplo");

        // Guardamos los eventos
        historialDominioRepository.save(historialDominio);
        historialDominioRepository.save(historialDominio1);

        entityManager.flush();

        // Recuperamos el historial
        List<HistorialDominio> listaHistorialDominio =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(dominio);

        // Comprobamos que hay dos eventos
        assertThat(listaHistorialDominio.size()).isEqualTo(2);

        // El evento más reciente debe aparecer primero
        assertThat(listaHistorialDominio.get(0).getTipoEvento())
                .isEqualTo(TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION);

        // El evento más antiguo debe aparecer segundo
        assertThat(listaHistorialDominio.get(1).getTipoEvento())
                .isEqualTo(TipoEventoDominio.AVISO_RENOVACION_ENVIADO);

        // Comprobamos el dominio asociado
        assertThat(listaHistorialDominio.get(0).getDominio().getNombreDominio())
                .isEqualTo("ana.com");
    }

    @Test
    void buscarHistorialDeUnDominio_noDevuelveEventosDeOtroDominio() {

        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        entityManager.persistAndFlush(cliente);

        // Primer dominio
        Dominio dominio1 = new Dominio();
        dominio1.setNombreDominio("ana.com");
        dominio1.setEstado(Estado.ACTIVO);
        dominio1.setCliente(cliente);
        dominio1.setRegistrador(Registrador.DOMITECA);

        entityManager.persistAndFlush(dominio1);

        // Segundo dominio
        Dominio dominio2 = new Dominio();
        dominio2.setNombreDominio("ejemplo.com");
        dominio2.setEstado(Estado.ACTIVO);
        dominio2.setCliente(cliente);
        dominio2.setRegistrador(Registrador.DOMITECA);

        entityManager.persistAndFlush(dominio2);

        // Historial del primer dominio
        HistorialDominio historial1 = new HistorialDominio();
        historial1.setDominio(dominio1);
        historial1.setTipoEvento(
                TipoEventoDominio.AVISO_RENOVACION_ENVIADO
        );
        historial1.setFecha(LocalDateTime.now());
        historial1.setDetalle("Aviso enviado");

        // Historial del segundo dominio
        HistorialDominio historial2 = new HistorialDominio();
        historial2.setDominio(dominio2);
        historial2.setTipoEvento(
                TipoEventoDominio.CLIENTE_ACEPTA_RENOVACION
        );
        historial2.setFecha(LocalDateTime.now());
        historial2.setDetalle("Cliente acepta");

        historialDominioRepository.save(historial1);
        historialDominioRepository.save(historial2);

        entityManager.flush();

        // Buscamos solamente el historial del primer dominio
        List<HistorialDominio> historialDominio1 =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(dominio1);

        // Comprobaciones
        assertThat(historialDominio1.size()).isEqualTo(1);

        assertThat(historialDominio1.get(0).getTipoEvento())
                .isEqualTo(TipoEventoDominio.AVISO_RENOVACION_ENVIADO);

        assertThat(historialDominio1.get(0).getDominio().getNombreDominio())
                .isEqualTo("ana.com");
    }
}