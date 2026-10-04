package books.domain.model;

public record AuthorQuery(String firstName, String lastName, String genre,
                          Integer page, int size, boolean sorted, boolean descending) {
    public AuthorQuery {
        if (page != null && (page < 0 || size < 1 || size > 200)) {
            throw new IllegalArgumentException("Page must be nonnegative and size between 1 and 200");
        }
    }
    public static AuthorQuery all() { return new AuthorQuery(null, null, null, null, 20, false, false); }
}
