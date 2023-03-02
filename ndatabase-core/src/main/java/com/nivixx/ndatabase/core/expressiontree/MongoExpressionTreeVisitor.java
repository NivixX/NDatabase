package com.nivixx.ndatabase.core.expressiontree;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.nivixx.ndatabase.api.exception.InvalidExpressionException;
import com.nivixx.ndatabase.api.model.NEntity;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.sql.SQLException;
import java.util.function.BiFunction;

public class MongoExpressionTreeVisitor <K,V extends NEntity<K>> extends ExpressionTreeVisitor<K,V> {

    private Bson bson;

    @Override
    public void visit(ExpressionTree<K, V> expressionTree) {
        bson = generateFilterRecursively(expressionTree.getRoot());
    }

    public Bson getBson() {
        return bson;
    }

    public Bson generateFilterRecursively(ExpressionTreeNode node) {
        if(node == null) { return null; }
        Bson left = generateFilterRecursively(node.left);
        String operator = node.token;
        Bson right = generateFilterRecursively(node.right);

        // BRACKETS HANDLING (OR / AND)
        BooleanBinaryOperator binaryOperator = BooleanBinaryOperator.fromToken(node.token);
        if(binaryOperator == BooleanBinaryOperator.AND) {
            return Filters.and(left, right);
        }
        if(binaryOperator == BooleanBinaryOperator.OR) {
            return Filters.or(left, right);
        }

        // OPERAND PATH CASE
        else if(node.token.startsWith("$.")) {
            return new Document("FIELD", node.token.substring(2));
        }
        else {
            // CONSTANT VALUE CASE
            if(binaryOperator == BooleanBinaryOperator.UNKNOW) {
                Document documentValueType = getDocumentValueType(node.token);
                return documentValueType;
            }
            // BOOLEAN BINARY OPERATOR CASE
            BsonDocument leftDoc = left.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry());
            BsonDocument rightDoc = right.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry());

            return getBooleanExpressionFilter(binaryOperator, leftDoc, rightDoc);
        }
    }

    private Bson getBooleanExpressionFilter(BooleanBinaryOperator binaryOperator, BsonDocument docLeft, BsonDocument docRight) {
        String fieldPath;
        Object value;
        if(docLeft.containsKey("FIELD")) {
            fieldPath = docLeft.get("FIELD").asString().getValue();
            value = extractValueFromDocument(docRight);
        }
        else {
            fieldPath = docRight.get("FIELD").asString().getValue();
            value = extractValueFromDocument(docLeft);
        }

        switch (binaryOperator) {
            case LESS_THAN:
                return Filters.lt(fieldPath, value);
            case LESS_THAN_OR_EQUALS:
                return Filters.lte(fieldPath, value);
            case GREATER_THAN:
                return Filters.gt(fieldPath, value);
            case GREATER_THAN_OR_EQUAL:
                return Filters.gte(fieldPath, value);
            case EQUALS:
                return Filters.eq(fieldPath, value);
            case DIFFERENT:
                return Filters.ne(fieldPath, value);
            default:
                throw new RuntimeException("failed to parse boolean binary operator for document");
        }
    }

    private Object extractValueFromDocument(BsonDocument document) {
        BsonValue value = document.get("VALUE");
        BsonType bsonType = value.getBsonType();
        switch (bsonType) {
            case DOUBLE:
                return value.asDouble().doubleValue();
            case STRING:
                return value.asString().getValue();
            case BOOLEAN:
                return value.asBoolean().getValue();
            case INT32:
                return value.asInt32().getValue();
            case INT64:
                return value.asInt64().getValue();
            case DECIMAL128:
                return value.asDecimal128().longValue();
            default:
                throw new IllegalArgumentException("Cannot parse document value " + document);
        }
    }

    private Document getDocumentValueType(String value) {

        try {
            double doubleValue = Double.parseDouble(value);
            return new Document("VALUE", doubleValue);
        }
        catch (NumberFormatException e) { }

        // Number
        try {
            long longValue = Long.parseLong(value);
            return new Document("VALUE", longValue);
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
            return new Document("VALUE", finalBooleanValue);
        }

        // String
        return new Document("VALUE", value);
    }
}
