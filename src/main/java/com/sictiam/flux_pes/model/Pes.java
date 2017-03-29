package com.sictiam.flux_pes.model;

import com.sictiam.flux_pes.command.AddAcknowledgmentOfReceipCommand;
import com.sictiam.flux_pes.command.CreatePesCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.common.IdentifierFactory;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Pes {

    @Id
    private String id;
    private String titre;

    protected Pes() {
        // this.id = IdentifierFactory.getInstance().generateIdentifier();
        this.id = UUID.randomUUID().toString();
    }

    public Pes(String id, String titre) {
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
        Pes pes = (Pes) o;
        return Objects.equals(id, pes.id) &&
            Objects.equals(titre, pes.titre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre);
    }

    @Override
    public String toString() {
        return "Pes{" +
            "id='" + id + '\'' +
            ", titre='" + titre + '\'' +
            '}';
    }

}
