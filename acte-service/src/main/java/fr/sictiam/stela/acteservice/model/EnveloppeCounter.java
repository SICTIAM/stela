package fr.sictiam.stela.acteservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class EnveloppeCounter implements Serializable {

    @Id
    private LocalDate date;
    private Integer counter;

    public EnveloppeCounter() {
    }

    public EnveloppeCounter(LocalDate date, Integer counter) {
        this.date = date;
        this.counter = counter;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
