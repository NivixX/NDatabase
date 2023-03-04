package com.nivixx.ndatabase.core.expressiontree;

import com.nivixx.ndatabase.api.model.NEntity;

public abstract class ExpressionTreeVisitor<K, V extends NEntity<K>> {

    public abstract void visit(ExpressionTree<K,V> expressionTree);

}