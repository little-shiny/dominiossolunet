package com.dominiossolunet.src.main.java.com.dominiossolunet.model;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.Registrador;
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

    // dice cuando fue el último envio y se usa para detectar si hay algún error en el cron.
    private LocalDate ultimoAviso;

    // evita reenviar un umbral mas de dos veces SOLO DEBUG
    private Integer ultimoUmbralAvisado;

    public Dominio(){}

}
