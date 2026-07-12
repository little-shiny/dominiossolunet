package com.dominiossolunet.src.main.java.com.dominiossolunet.model;


import com.dominiossolunet.model.Dominio;
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
    private int id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    @NotNull
    @Column(nullable = false)
    private Dominio dominio;

    @NotNull
    @Column(nullable = false)
    private String token;

    @NotNull
    @Column(nullable = false)
    private boolean usado;

    public TokenRenovacion(){}
}