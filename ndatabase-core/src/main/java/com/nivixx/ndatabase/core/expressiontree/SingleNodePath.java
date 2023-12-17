package com.nivixx.ndatabase.core.expressiontree;

public class SingleNodePath {

    private Class<?> type;
    private String pathName;
    private SingleNodePath child;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public SingleNodePath() {
        this.pathName = "";
    }

    public SingleNodePath(String pathName) {
        this.pathName = pathName;
    }

    public SingleNodePath getChild() {
        return child;
    }

    public void setChild(SingleNodePath child) {
        this.child = child;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }


    public String getPathName() {
        return pathName;
    }


    /**
     * Return a full path such as field1.subField2.subField3
     * @param pathSeparatorSymbol the symbol for each field name separation
     * @return full path calculated recursively by aggregating children nodes
     */
    public String getFullPath(String pathSeparatorSymbol) {
        SingleNodePath currentNode = this;
        String fullFieldPath = "";
        while(currentNode != null) {
            fullFieldPath = fullFieldPath.isEmpty() ? currentNode.getPathName() :
                    fullFieldPath + pathSeparatorSymbol + currentNode.getPathName();
            currentNode = currentNode.getChild();
        }
        return fullFieldPath;
    }

    /**
     * @return the type of the latest children (the field we are targeting)
     */
    public Class<?> getLastNodeType() {
        SingleNodePath currentNode = this;
        Class<?> fieldType = null;
        while(currentNode != null) {
            currentNode = currentNode.getChild();
            if(currentNode != null) {
                fieldType = currentNode.getType();
            }
        }
        return fieldType;
    }

}
