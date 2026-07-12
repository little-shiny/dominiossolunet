package com.dominiossolunet.src.main.java.com.dominiossolunet.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

/**
 * Model Class that represents a Client data type
 */

@Entity
@Getter @Setter
public class Cliente {

    @Id @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String email;

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
