package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.HistorialDominioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionDominioServiceTest {

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private HistorialDominioRepository historialDominioRepository;

    @InjectMocks
    private GestionDominioService gestionDominioService;


    @Test
    void marcarComoRenovado_dominioExistente_cambiaEstadoYRegistraHistorial() {

        // Arrange
        Dominio dominio = crearDominio();

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        // Act
        gestionDominioService.marcarComoRenovado(1);

        // Assert
        assertThat(dominio.getEstado())
                .isEqualTo(Estado.ACTIVO);

        ArgumentCaptor<HistorialDominio> captor =
                ArgumentCaptor.forClass(HistorialDominio.class);

        verify(historialDominioRepository)
                .save(captor.capture());

        HistorialDominio historial = captor.getValue();

        assertThat(historial.getDominio())
                .isSameAs(dominio);

        assertThat(historial.getTipoEvento())
                .isEqualTo(
                        com.dominiossolunet.model.enums.TipoEventoDominio.RENOVACION_REALIZADA
                );

        assertThat(historial.getFecha())
                .isNotNull();

        assertThat(historial.getDetalle())
                .isEqualTo("Renovación realizada en el registrador");
    }


    @Test
    void marcarComoRenovado_dominioNoExiste_lanzaExcepcion() {

        // Arrange
        when(dominioRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                gestionDominioService.marcarComoRenovado(999)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dominio no encontrado: 999");

        verify(historialDominioRepository, never())
                .save(any());
    }


    @Test
    void marcarComoRenovado_dominioYaActivo_lanzaExcepcion() {

        // Arrange
        Dominio dominio = crearDominio();
        dominio.setEstado(Estado.ACTIVO);

        when(dominioRepository.findById(1))
                .thenReturn(Optional.of(dominio));

        // Act + Assert
        assertThatThrownBy(() ->
                gestionDominioService.marcarComoRenovado(1)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El dominio ya está activo: ana.com");

        verify(historialDominioRepository, never())
                .save(any());
    }


    private Dominio crearDominio() {

        Dominio dominio = new Dominio();

        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.AVISO_ENVIADO);
        dominio.setRegistrador(Registrador.DOMITECA);

        return dominio;
    }
}