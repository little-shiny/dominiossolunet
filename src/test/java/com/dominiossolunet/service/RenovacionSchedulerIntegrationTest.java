package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.model.enums.TipoEventoDominio;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import com.dominiossolunet.scheduler.RenovacionScheduler;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RenovacionSchedulerIntegrationTest.MailTestConfiguration.class)
class RenovacionSchedulerIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private RenovacionScheduler scheduler;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private HistorialDominioRepository historialDominioRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        registry.add(
                "renovacion.umbrales",
                () -> "30,15,5,1"
        );

        registry.add(
                "app.url.renovacion",
                () -> "http://localhost/renovacion/"
        );

        registry.add(
                "spring.mail.username",
                () -> "test@solunet.es"
        );

        registry.add(
                "app.email.admin",
                () -> "admin@solunet.es"
        );

        registry.add(
                "renovacion.scheduler.cron",
                () -> "-"
        );
    }

    @BeforeEach
    void limpiarDatos() throws FolderException {

        historialDominioRepository.deleteAll();
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();

        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void ejecutarRenovaciones_debeEjecutarElProcesoCompleto()
            throws Exception {

        // ---------------------------------------------------------
        // ARRANGE
        // ---------------------------------------------------------

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

        // ---------------------------------------------------------
        // ACT
        // ---------------------------------------------------------

        scheduler.ejecutarRenovaciones();

        // ---------------------------------------------------------
        // ASSERT - DOMINIO
        // ---------------------------------------------------------

        Dominio dominioActualizado =
                dominioRepository
                        .findById(dominio.getId())
                        .orElseThrow();

        assertThat(dominioActualizado.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominioActualizado.getUltimoUmbralAvisado())
                .isEqualTo(30);

        assertThat(dominioActualizado.getUltimoAviso())
                .isEqualTo(LocalDate.now());

        // ---------------------------------------------------------
        // ASSERT - HISTORIAL
        // ---------------------------------------------------------

        List<HistorialDominio> historiales =
                historialDominioRepository
                        .findByDominioOrderByFechaDesc(
                                dominioActualizado
                        );

        assertThat(historiales)
                .hasSize(1);

        assertThat(historiales.getFirst().getTipoEvento())
                .isEqualTo(
                        TipoEventoDominio.AVISO_RENOVACION_ENVIADO
                );

        assertThat(historiales.getFirst().getDetalle())
                .isEqualTo(
                        "Aviso de renovación enviado al cliente"
                );

        // ---------------------------------------------------------
        // ASSERT - EMAIL
        // ---------------------------------------------------------

        assertThat(greenMail.getReceivedMessages())
                .hasSize(2);

        assertThat(greenMail.getReceivedMessages()[0].getAllRecipients())
                .isNotEmpty();
    }

    @TestConfiguration
    static class MailTestConfiguration {

        @Bean
        @Primary
        JavaMailSenderImpl testMailSender() {

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

            mailSender.setHost("localhost");
            mailSender.setPort(ServerSetupTest.SMTP.getPort());
            mailSender.setUsername("test@solunet.es");

            Properties properties = mailSender.getJavaMailProperties();

            properties.setProperty("mail.smtp.auth", "false");
            properties.setProperty("mail.smtp.starttls.enable", "false");
            properties.setProperty("mail.smtp.starttls.required", "false");

            return mailSender;
        }
    }
}