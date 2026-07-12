package com.dominiossolunet.model;

import com.dominiossolunet.model.enums.EstadoFacturacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter@Setter
public class Facturacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @NotNull
    private int id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EstadoFacturacion estadoFacturacion;

    private LocalDate fechaUltimaFactura;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "id_dominio")
    @NotNull
    private Dominio dominio;
    private String nota;

    public Facturacion(){}
}
