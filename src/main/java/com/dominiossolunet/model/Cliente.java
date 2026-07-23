package com.dominiossolunet.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Model Class that represents a Client data type
 */

@Entity
@Getter @Setter
public class Cliente {

    @Id @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String email;

    @OneToMany(mappedBy = "cliente")
    private List<Dominio> dominios;

    public Cliente(){}

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + nombre + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
