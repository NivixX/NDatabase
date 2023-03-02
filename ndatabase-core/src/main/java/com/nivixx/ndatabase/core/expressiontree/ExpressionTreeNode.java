package com.nivixx.ndatabase.core.expressiontree;


public class ExpressionTreeNode {

    public String token;
    public ExpressionTreeNode left, right;
    public ExpressionTreeNode(String token) {
        this.token = token;
        this.left = this.right = null;
    }

}