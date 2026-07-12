package com.dominiossolunet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Dominio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @NotNull
    private int id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Estado estado;

    private LocalDate fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private Cliente cliente;

    @NotNull
    private String nombreDominio;

    @NotNull
    private Registrador registrador;

    private LocalDate ultimoAviso;

    private Integer ultimoUmbralAvisado;

    public Dominio(){}

}
