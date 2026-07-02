package com.dominiossolunet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
public class Dominio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Setter
    private Estado estado;

    @Setter
    private LocalDate fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente")
    @Setter
    private Cliente cliente;

    @Setter
    private String nombreDominio;

    @Setter
    private LocalDate ultimoAviso;

}
