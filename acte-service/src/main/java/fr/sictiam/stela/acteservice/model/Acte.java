package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Acte {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uuid;

    @Column(unique=true)
    private String numero;

    public Acte() {
    }

    public Acte(String numero) {
        this.numero = numero;
    }

    public Long getUuid() {
        return this.uuid;
    }

    public String getNumero() {
        return this.numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}