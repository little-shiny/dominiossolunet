package com.dominiossolunet.model;

import com.dominiossolunet.model.enums.TipoEventoDominio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Clase que representa un registro histórico de un determinado dominio
 * ManyToOne porque un dominio puede tener muchos acontecimientos
 */
@Getter
@Setter
@Entity
public class HistorialDominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @NotNull
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio", nullable = false)
    private Dominio dominio;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoEventoDominio tipoEvento;

    @NotNull
    private LocalDateTime fecha;

    private String detalle;

    public HistorialDominio() {
    }
}