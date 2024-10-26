package com.nivixx.ndatabase.expressiontree.visitor;

import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.expressiontree.ExpressionTree;

public abstract class ExpressionTreeVisitor<K, V extends NEntity<K>> {

    public abstract void visit(ExpressionTree<K,V> expressionTree);

}