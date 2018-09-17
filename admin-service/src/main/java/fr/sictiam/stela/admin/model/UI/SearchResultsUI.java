package fr.sictiam.stela.admin.model.UI;

import java.util.List;

public class SearchResultsUI {

    private Long totalCount;
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
