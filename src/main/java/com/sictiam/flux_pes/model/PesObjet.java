package com.sictiam.flux_pes.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/**
 * Created by s.vergon on 24/03/2017.
 */
@Entity
public class PesObjet {
    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    private String titre;

    //protected PesObjet() {}

    public PesObjet(Integer id,String titre) {
        this.id = id;
        this.titre = titre;
    }

    public String toString() {
        String texte = this.id+"   "+this.titre;
        return texte;
        //enfin faire une méthode get et set pour prénom ca serait cool

    }

}
