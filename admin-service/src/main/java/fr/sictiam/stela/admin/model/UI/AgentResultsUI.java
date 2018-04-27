package fr.sictiam.stela.admin.model.UI;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.Agent;

import java.util.List;

public class AgentResultsUI {
    @JsonView(Views.AgentViewPublic.class)
    private Long totalCount;
    @JsonView(Views.AgentViewPublic.class)
    private List<Agent> results;

    public AgentResultsUI() {
    }

    public AgentResultsUI(Long totalCount, List<Agent> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public List<Agent> getResults() {
        return results;
    }
}
