package advisor.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Pageable<T> {
    private final List<T> results;
    @Getter
    private int pageNumber = 1;
    private final int pageSize;
    @Getter
    private int totalPages;

    public Pageable(List<T> results, int pageSize) {
        this.results = results;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) results.size() / pageSize);
    }

    public boolean hasNext() {
        return ((pageNumber + 1) * pageSize) <= results.size();
    }

    public boolean hasPrev() {
        return (pageNumber > 1);
    }

    public List<T> getPage() {
        return results.subList(
                Math.max(0, (pageNumber - 1) * pageSize),
                Math.min(pageNumber * pageSize, results.size()));
    }

    public void inc() {
        if (hasNext()) pageNumber++;
    }

    public void dec() {
        if (hasPrev()) pageNumber--;
    }
}