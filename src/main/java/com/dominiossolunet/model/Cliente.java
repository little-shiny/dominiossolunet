package com.dominiossolunet.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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

    @Column(nullable = false)
    private String nombre;

    private String email;

    @OneToMany(mappedBy = "cliente")
    List<Dominio> dominios;

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
