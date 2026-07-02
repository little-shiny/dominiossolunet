package com.dominiossolunet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
public class Facturacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @Enumerated(EnumType.STRING)
    @Setter@Getter
    private EstadoFacturacion estadoFacturacion;

    @Setter@Getter
    private LocalDate fechaUltimaFactura;

    @Setter@Getter
    private int idCliente;

    @Setter@Getter
    private int id_dominio;

    @Setter@Getter
    private String nota;
}
