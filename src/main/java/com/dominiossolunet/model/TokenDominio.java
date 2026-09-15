package com.dominiossolunet.model;

import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa la relación entre un token y un dominio de un TokenCliente.
 * Es una tabla intermedia con el estado de los dominios de cada aviso
 */

@Entity
@Getter
@Setter
public class TokenDominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_token", nullable = false)
    private TokenCliente tokenCliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio", nullable = false)
    private Dominio dominio;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoAvisoRenovacion estadoAvisoRenovacion;

    private LocalDateTime fechaInteraccionCliente;
}
