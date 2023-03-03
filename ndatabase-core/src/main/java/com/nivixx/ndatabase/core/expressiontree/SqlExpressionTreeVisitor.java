package com.nivixx.ndatabase.core.expressiontree;

import com.nivixx.ndatabase.api.exception.InvalidExpressionException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SqlExpressionTreeVisitor<K,V extends NEntity<K>> extends ExpressionTreeVisitor<K,V> {

    private String sqlQuery;
    private List<Consumer<PreparedStatement>> preparedStatementConsumers;
    private int currentPreparedStatmentIndex;

    public SqlExpressionTreeVisitor(String sqlQuery) {
        this.sqlQuery = sqlQuery;
        this.preparedStatementConsumers = new ArrayList<>();
        this.currentPreparedStatmentIndex = 1;
    }

    public PreparedStatement getPreparedStatement(Connection connection) throws NDatabaseException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sqlQuery);
        } catch (SQLException e) {
            throw new InvalidExpressionException(e);
        }
        for (Consumer<PreparedStatement> preparedStatmentConsumer : preparedStatementConsumers) {
            preparedStatmentConsumer.accept(preparedStatement);
        }
        return preparedStatement;
    }

    @Override
    public void visit(ExpressionTree<K,V> expressionTree) {
        String query = parseIntoQueryRecursively(expressionTree.getRoot());
        sqlQuery = sqlQuery + query;
    }

    private String parseIntoQueryRecursively(ExpressionTreeNode node) {
        if(node == null) { return ""; }
        String left = parseIntoQueryRecursively(node.left);
        String operator = node.token;
        String right = parseIntoQueryRecursively(node.right);

        // BRACKETS HANDLING (OR / AND)
        BooleanBinaryOperator binaryOperator = BooleanBinaryOperator.fromToken(node.token);
        if(binaryOperator == BooleanBinaryOperator.AND || binaryOperator == BooleanBinaryOperator.OR) {
            return "(" + left + ") " + getBinaryOperatorToken(binaryOperator) + " (" +right + ")";
        }

        // OPERAND PATH CASE
        else if(node.token.startsWith("$.")) {
            String[] paths = node.token.substring(2).split("\\.");
            return left + " " + getPathToken(node.token) + " " + right;
        }
        else {
            // CONSTANT VALUE CASE
            if(binaryOperator == BooleanBinaryOperator.UNKNOW) {
                addStatementConsumer(node.token);
                return left + " " + "?" + " " + right;
            }
            // BOOLEAN BINARY OPERATOR CASE
            return left + " " + getBinaryOperatorToken(binaryOperator) + " " + right;
        }
    }

    private String getPathToken(String path) {
        // start with $.
        // column names cannot contains . so we use _
        return path.substring(2).replaceAll("\\.","_");
    }

    private String getBinaryOperatorToken(BooleanBinaryOperator binaryOperator) {
        String binaryOperatorToken = null;
        switch (binaryOperator) {
            case LESS_THAN:
                binaryOperatorToken = "<";
                break;
            case LESS_THAN_OR_EQUALS:
                binaryOperatorToken = "<=";
                break;
            case GREATER_THAN:
                binaryOperatorToken = ">";
                break;
            case GREATER_THAN_OR_EQUAL:
                binaryOperatorToken = ">=";
                break;
            case EQUALS:
                binaryOperatorToken = "=";
                break;
            case DIFFERENT:
                binaryOperatorToken = "!=";
                break;
            case AND:
                binaryOperatorToken = "AND";
                break;
            case OR:
                binaryOperatorToken = "OR";
                break;
            case UNKNOW:
                break;
        }
        return binaryOperatorToken;
    }

    private void addStatementConsumer(String value) {
        // Decimal number
        try {
            double doubleValue = Double.parseDouble(value);
            preparedStatementConsumers.add(ps -> {
                try {
                    ps.setDouble(currentPreparedStatmentIndex, doubleValue);
                    currentPreparedStatmentIndex++;
                } catch (SQLException e) {
                    throw new InvalidExpressionException(e);
                }
            });
            return;
        }
        catch (NumberFormatException e) { }

        // Number
        try {
            long longValue = Long.parseLong(value);
            preparedStatementConsumers.add(ps -> {
                try {
                    ps.setLong(currentPreparedStatmentIndex, longValue);
                    currentPreparedStatmentIndex++;
                } catch (SQLException e) {
                    throw new InvalidExpressionException(e);
                }
            });
            return;
        }
        catch (NumberFormatException e) { }

        // boolean
        Boolean booleanValue = null;
        if("true".equals(value)) {
            booleanValue = true;
        }
        if("false".equals(value)) {
            booleanValue = false;
        }
        if(booleanValue != null) {
            boolean finalBooleanValue = booleanValue;
            preparedStatementConsumers.add(ps -> {
                try {
                    ps.setBoolean(currentPreparedStatmentIndex, finalBooleanValue);
                    currentPreparedStatmentIndex++;
                } catch (SQLException e) {
                    throw new InvalidExpressionException(e);
                }
            });
            return;
        }

        // string
        preparedStatementConsumers.add(ps -> {
            try {
                ps.setString(currentPreparedStatmentIndex, value);
                currentPreparedStatmentIndex++;
            } catch (SQLException e) {
                throw new InvalidExpressionException(e);
            }
        });
    }
}
