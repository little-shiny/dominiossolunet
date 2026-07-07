package com.dominiossolunet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter@Setter
public class Facturacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private EstadoFacturacion estadoFacturacion;
    private LocalDate fechaUltimaFactura;
    private int idCliente;
    private int idDominio;
    private String nota;
}
