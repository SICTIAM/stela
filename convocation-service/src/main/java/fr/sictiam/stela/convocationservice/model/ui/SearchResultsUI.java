package fr.sictiam.stela.convocationservice.model.ui;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsUI {
    private Long totalCount;
    private List<?> results;

    public SearchResultsUI() {
        totalCount = 0L;
        results = new ArrayList<>();
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
