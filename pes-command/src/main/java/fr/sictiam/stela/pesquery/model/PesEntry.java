package fr.sictiam.stela.pesquery.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class PesEntry {

    @Id
    @GeneratedValue
    private long id;
    private String pesId;

    public PesEntry() {
    }

    public PesEntry(String pesId) {
        this.pesId = pesId;
    }

    public long getId() {
        return id;
    }

    public String getPesId() {
        return pesId;
    }
}
