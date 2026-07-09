package com.dominiossolunet.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;


import java.util.List;

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

    @NotBlank
    private String nombre;

    @NotBlank
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
