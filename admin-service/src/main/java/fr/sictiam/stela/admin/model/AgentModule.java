package fr.sictiam.stela.admin.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Entity
public class AgentModule implements Serializable {

    @Embeddable
    public static class AgentModuleId implements Serializable {
        @Column(name = "agent_uuid")
        private String agentUuid;
        @Column(name = "local_authority_uuid")
        private String localAuthorityUuid;

        public AgentModuleId() {
        }

        public AgentModuleId(String agentUuid, String localAuthorityUuid) {
            this.agentUuid = agentUuid;
            this.localAuthorityUuid = localAuthorityUuid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AgentModuleId that = (AgentModuleId) o;

            if (!agentUuid.equals(that.agentUuid)) return false;
            return localAuthorityUuid.equals(that.localAuthorityUuid);
        }

        @Override
        public int hashCode() {
            int result = agentUuid.hashCode();
            result = 31 * result + localAuthorityUuid.hashCode();
            return result;
        }
    }

    @EmbeddedId
    private AgentModuleId id;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private LocalAuthority localAuthority;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private Agent agent;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Module> modules;

    private Date creationDate;

    public AgentModule() {
    }

    public AgentModule(LocalAuthority localAuthority, Agent agent, Module module) {
        this.id = new AgentModuleId(agent.getUuid(), localAuthority.getUuid());
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.modules = Collections.singleton(module);
        this.creationDate = new Date();

        agent.addModule(this);
        localAuthority.addAgent(this);
    }

    public AgentModule(LocalAuthority localAuthority, Agent agent, Set<Module> modules) {
        this.id = new AgentModuleId(agent.getUuid(), localAuthority.getUuid());
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.modules = modules;
        this.creationDate = new Date();

        agent.addModule(this);
        localAuthority.addAgent(this);
    }

    public AgentModuleId getId() {
        return id;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public Agent getAgent() {
        return agent;
    }

    public Set<Module> getModules() {
        return modules;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public String toString() {
        return "AgentModule{" +
                "localAuthority=" + localAuthority +
                ", agent=" + agent +
                ", module=" + modules +
                ", createdDate=" + creationDate +
                '}';
    }
}
