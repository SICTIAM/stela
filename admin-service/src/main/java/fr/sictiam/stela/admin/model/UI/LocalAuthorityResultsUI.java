package fr.sictiam.stela.admin.model.UI;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.LocalAuthority;

import java.util.List;

public class LocalAuthorityResultsUI {
    @JsonView(Views.LocalAuthorityView.class)
    private Long totalCount;
    @JsonView(Views.LocalAuthorityView.class)
    private List<LocalAuthority> results;

    public LocalAuthorityResultsUI() {
    }

    public LocalAuthorityResultsUI(Long totalCount, List<LocalAuthority> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public List<LocalAuthority> getResults() {
        return results;
    }
}
