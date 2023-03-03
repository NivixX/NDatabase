package com.nivixx.ndatabase.api.query;

import java.util.ArrayList;
import java.util.List;

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

}
