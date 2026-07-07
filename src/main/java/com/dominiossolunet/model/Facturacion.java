package com.dominiossolunet.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter@Setter
public class Facturacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @Enumerated(EnumType.STRING)
    private EstadoFacturacion estadoFacturacion;
    private LocalDate fechaUltimaFactura;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "id_dominio")
    private Dominio dominio;
    private String nota;
}
