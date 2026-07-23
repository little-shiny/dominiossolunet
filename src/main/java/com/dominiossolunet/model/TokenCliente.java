package com.dominiossolunet.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Clase modelo que representa "Una tanda" de dominios para avisar al cliente, es decir, se emplea un token por
 * cliente para todos sus dominios y no un token por cada dominio
 */
@Entity @Getter@Setter
public class TokenCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(AccessLevel.NONE)
    private int id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente",nullable = false)
    @NotNull
    private Cliente cliente;

    @NotNull
    @Column(nullable = false)
    private String token;

    @NotNull
    @Column(nullable = false)
    private boolean usado;

    public TokenCliente(){}
}