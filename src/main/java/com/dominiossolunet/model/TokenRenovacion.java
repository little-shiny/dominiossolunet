package com.dominiossolunet.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Getter@Setter
public class TokenRenovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(AccessLevel.NONE)
    @NotNull
    private int id;

    @NotNull
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    @NotNull
    private Dominio dominio;

    @NotNull
    private String token;

    @NotNull
    private boolean usado;

    public TokenRenovacion(){}
}