package com.nivixx.ndatabase.api.query;

public class NQuery {

    public static class Predicate {
        private final String predicate;

        private Predicate(String expression) {
            this.predicate = expression;
        }

        public String getPredicate() {
            return predicate;
        }

    }

    public static Predicate predicate(String expression) {
        return new Predicate(expression);
    }
}
