package com.dominiossolunet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Model Class that represents a Client data type
 */

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter@Setter
    private int id;

    @Getter@Setter
    private String nombre;

    @Getter@Setter
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
