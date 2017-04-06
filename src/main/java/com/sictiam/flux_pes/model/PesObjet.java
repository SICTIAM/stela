package com.sictiam.flux_pes.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

@Entity
public class PesObjet {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String titre;

    //protected PesObjet() {}
    protected PesObjet() {}


    public PesObjet(String titre) {
            //this.id = id;
            this.titre = titre;
        }
    public PesObjet(Integer id, String titre) {
        this.id = id;
        this.titre = titre;
    }


}
