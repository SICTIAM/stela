package fr.sictiam.stela.acteservice.model.ui;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

public class SearchResultsUI {

    @JsonView(Views.SearchResultFullView.class)
    private Long totalCount;
    @JsonView(Views.SearchResultFullView.class)
    private List<?> results;

    public SearchResultsUI() {
    }

    public SearchResultsUI(Long totalCount, List<?> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public List<?> getResults() {
        return results;
    }
}
