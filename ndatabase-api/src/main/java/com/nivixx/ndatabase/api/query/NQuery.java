package com.nivixx.ndatabase.api.query;

/**
 * This class is used to query the database using a NQuery
 * See NQuery documentation <a href="https://github.com/NivixX/NDatabase/wiki">here</a>
 */
public class NQuery {

    /**
     * Predicate class that contains the query expression
     */
    public static class Predicate {
        private final String predicate;

        private Predicate(String expression) {
            this.predicate = expression;
        }

        public String getPredicate() {
            return predicate;
        }

    }

    /**
     *
     * @param expression NQuery expression string, this expression will be
     *                   parsed as binary tree to work on different database provider
     *                   such as MongoDB, MySQL, ...
     * @return Predicate that represent the expression
     *
     * Read more about NQuery: <a href="https://github.com/NivixX/NDatabase/wiki">here</a>
     * Example:
     * <pre>{@code
     *     NQuery.predicate("$.discordId == 3432487284963298");
     * }</pre>
     */
    public static Predicate predicate(String expression) {
        return new Predicate(expression);
    }
}
