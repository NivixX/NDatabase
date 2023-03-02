package com.nivixx.ndatabase.core.expressiontree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum BooleanBinaryOperator {
    LESS_THAN,
    LESS_THAN_OR_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    EQUALS,
    DIFFERENT,
    AND,
    OR,
    UNKNOW;

    private static final Set<String> booleanOperators =
            new HashSet<>(Arrays.asList(
                    "(",")",
                    "||", "&&",
                    "!=","==","<","<=",">",">="
            ));

    static boolean isAssignable(String token) {
        return booleanOperators.contains(token);
    }

    static BooleanBinaryOperator fromToken(String token) {
        switch(token) {
            case "<":
                return LESS_THAN;
            case "<=":
                return LESS_THAN_OR_EQUALS;
            case ">":
                return GREATER_THAN;
            case ">=":
                return GREATER_THAN_OR_EQUAL;
            case "==":
                return EQUALS;
            case "!=":
                return DIFFERENT;
            case "&&":
                return AND;
            case "||":
                return OR;

        }
        return UNKNOW;
    }
}