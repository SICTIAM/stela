package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.StatusType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public class ActeUuidsAndSearchUI {

    private List<String> uuids;
    private String multifield;
    private String number;
    private String objet;
    private ActeNature nature;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionTo;
    private StatusType status;

    public ActeUuidsAndSearchUI() {
    }

    public ActeUuidsAndSearchUI(List<String> uuids) {
        this.uuids = uuids;
    }

    public List<String> getUuids() {
        return uuids;
    }

    public String getMultifield() {
        return multifield;
    }

    public String getNumber() {
        return number;
    }

    public String getObjet() {
        return objet;
    }

    public ActeNature getNature() {
        return nature;
    }

    public LocalDate getDecisionFrom() {
        return decisionFrom;
    }

    public LocalDate getDecisionTo() {
        return decisionTo;
    }

    public StatusType getStatus() {
        return status;
    }
}
