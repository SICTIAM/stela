package fr.sictiam.stela.admin.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

@Entity
public class AgentModule implements Serializable {

    @Id
    @ManyToOne
    private LocalAuthority localAuthority;
    @Id
    @ManyToOne
    private Agent agent;
    @Id
    private Module module;
    private Date createdDate;

    public AgentModule(LocalAuthority localAuthority, Agent agent, Module module) {
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.module = module;
        this.createdDate = new Date();
    }
}
