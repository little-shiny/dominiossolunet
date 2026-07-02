package com.dominiossolunet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Model Class that represents a Client data type
 */

@Entity
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter@Setter
    private int id;

    @Getter@Setter
    private String name;

    @Getter@Setter
    private String email;

    public Client(){}

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
