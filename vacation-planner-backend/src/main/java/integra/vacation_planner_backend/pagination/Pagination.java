package integra.vacation_planner_backend.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class Pagination {

    private static final Set<Integer> ALLOWED_SIZES =
        Set.of(5, 10, 20, 50, 100);

    private Pagination() {
    }

    public static PageRequest create(int page, int size, Sort sort) {
        if (page < 0) {
            throw new IllegalArgumentException(
                "page must be greater than or equal to 0"
            );
        }

        if (!ALLOWED_SIZES.contains(size)) {
            throw new IllegalArgumentException(
                "size must be one of: 5, 10, 20, 50, 100"
            );
        }

        return PageRequest.of(page, size, sort);
    }
}
