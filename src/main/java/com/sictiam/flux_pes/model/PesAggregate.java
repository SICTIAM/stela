package com.sictiam.flux_pes.model;

import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;

import java.util.Objects;
import java.util.UUID;

@AggregateRoot
public class PesAggregate {

    @AggregateIdentifier
    private String id;
    private String titre;


    protected PesAggregate() {
        // this.id = IdentifierFactory.getInstance().generateIdentifier();
        this.id = UUID.randomUUID().toString();
    }

    public PesAggregate(String id, String titre) {
        this.id = id;
        this.titre = titre;
    }

    public String getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PesAggregate pes = (PesAggregate) o;
        return Objects.equals(id, pes.id) &&
            Objects.equals(titre, pes.titre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre);
    }

    @Override
    public String toString() {
        return "{" +
            "id='" + id + '\'' +
            ", titre='" + titre + '\'' +
            '}';
    }

}
