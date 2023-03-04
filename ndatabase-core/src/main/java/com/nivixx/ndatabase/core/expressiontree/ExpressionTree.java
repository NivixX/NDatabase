package com.nivixx.ndatabase.core.expressiontree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.reflection.NReflectionUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExpressionTree<K,V extends NEntity<K>> {

    private final Class<V> nEntityClass;
    private final ExpressionTreeNode root;

    public ExpressionTree(Class<V> nEntityClass, ExpressionTreeNode firstNode) {
        this.nEntityClass = nEntityClass;
        this.root = firstNode;
    }

    public static <K,V extends NEntity<K>> ExpressionTree<K,V> fromExpressionString(String expression, Class<V> nEntityClass) {
        List<String> args = splitExpression(expression);
        ExpressionTreeNode node;
        try {
            node = build(args);
        } catch (Exception e) {
            throw new NDatabaseException("Failed to parse expression because was invalid : " + expression, e);
        }

        ExpressionTree<K, V> expressionTree = new ExpressionTree<>(nEntityClass, node);
        expressionTree.validateNEntityPath(expressionTree.getRoot());
        return expressionTree;
    }

    private void validateNEntityPath(ExpressionTreeNode root) {
        if (root != null) {
            validateNEntityPath(root.left);
            if(root.token.startsWith("$.")) {
                String[] paths = root.token.substring(2).split("\\.");
                if(!validateNEntityDeclaration(paths, nEntityClass)) {
                    throw new NDatabaseException(root.token + " doesn't exist in object" + nEntityClass);
                }
            }
            validateNEntityPath(root.right);
        }
    }

    private boolean validateNEntityDeclaration(String[] paths, Class<?> classType) {
        Field[] declaredFields = classType.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String jsonFieldName = NReflectionUtil.resolveJsonFieldName(declaredField);

            if(classType.getPackage().getName().startsWith("java.")) {
                continue;
            }
            if(paths[0].equals(jsonFieldName)) {
                if(paths.length == 1) {
                    if(!declaredField.isAnnotationPresent(Indexed.class)) {
                        throw new NDatabaseException("the field " + jsonFieldName + " of class " + classType + "is not indexed, add @Indexed annotation if you want to query this class");
                    }
                    return true;
                }
                else {
                    String[] trimmedPath = new String[paths.length-1];
                    System.arraycopy(paths, 1, trimmedPath, 0, paths.length - 1);
                    if(validateNEntityDeclaration(trimmedPath, declaredField.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<String> operandChars = Arrays.asList("$","==","!=","||","&&",">=", "<=", ">","<", "(",")");

    private static List<String> splitExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        String remainingExpression = expression;
        while(remainingExpression.contains(" ")) {
            remainingExpression = remainingExpression.replaceAll(" ", "");
        }
        String reader = "";
        int readerEndIndex = 0;
        boolean isLast = false;
        while(!isLast) {
            reader = remainingExpression.substring(0, Math.min(readerEndIndex, remainingExpression.length()));
            if(readerEndIndex > remainingExpression.length()) {
                isLast = true;
                tokens.add(reader);
            }

            AtomicReference<String> finalReader1 = new AtomicReference<>(reader);
            Optional<String> matchOperand = operandChars.stream()
                    .filter(s -> finalReader1.get().endsWith(s))
                    .findFirst();
            if(matchOperand.isPresent()) {
                String token = matchOperand.get();
                if(readerEndIndex > matchOperand.get().length()) {
                    readerEndIndex = readerEndIndex - matchOperand.get().length();
                    reader = remainingExpression.substring(0,readerEndIndex);
                }
                if(("<".equals(token) || ">".equals(token))
                        && readerEndIndex+1 <= remainingExpression.length()
                        && "=".equals(remainingExpression.substring(readerEndIndex,readerEndIndex+1))) {
                    tokens.add(token + "=");
                    readerEndIndex++;
                }
                else if(reader.startsWith("$")) {
                    AtomicReference<String> finalReader = new AtomicReference<>(reader.substring(1));
                    matchOperand = Optional.empty();
                    while (!matchOperand.isPresent()) {
                        readerEndIndex++;
                        finalReader.set(remainingExpression.substring(0, readerEndIndex));
                        matchOperand = operandChars.stream()
                                .filter(s -> finalReader.get().endsWith(s))
                                .findFirst();
                    }
                    readerEndIndex = readerEndIndex - matchOperand.get().length();
                    tokens.add(finalReader.get().substring(0,finalReader.get().length()-matchOperand.get().length()));
                }
                else {
                    tokens.add(reader);
                }
                remainingExpression = remainingExpression.substring(readerEndIndex);
                readerEndIndex = 0;
            }
            readerEndIndex++;
        }
        return tokens;
    }



    private static ExpressionTreeNode build(List<String> expression) {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        Stack<ExpressionTreeNode> numStack = new Stack<>();
        Stack<String> opStack = new Stack<>();
        for (String token : expression) {
            if (!BooleanBinaryOperator.isAssignable(token)) {
                numStack.push(new ExpressionTreeNode(token));
            } else if (token.equals("(")) {
                opStack.push(token);
            } else if (token.equals(")")) {
                while (!opStack.peek().equals("(")) {
                    numStack.push(buildNode(numStack.pop(), numStack.pop(), opStack.pop()));
                }
                opStack.pop();
            } else {
                while (!opStack.isEmpty() && getOperatorPriority(opStack.peek()) >= getOperatorPriority(token)) {
                    numStack.push(buildNode(numStack.pop(), numStack.pop(), opStack.pop()));
                }
                opStack.push(token);
            }
        }
        while (!opStack.isEmpty()) {
            numStack.push(buildNode(numStack.pop(), numStack.pop(), opStack.pop()));
        }
        return numStack.isEmpty() ? null : numStack.pop();
    }

    private static int getOperatorPriority(String operator) {
        switch (operator) {
            case "(":
                return 0;
            case "||":
                return 1;
            case "&&":
                return 2;
            case "!=":
                return 3;
            default:
                return 4;
        }
    }

    private static ExpressionTreeNode buildNode(ExpressionTreeNode node2, ExpressionTreeNode node1, String op) {
        ExpressionTreeNode root = new ExpressionTreeNode(op);
        root.left = node1;
        root.right = node2;
        return root;
    }

    public ExpressionTreeNode getRoot() {
        return root;
    }
}
