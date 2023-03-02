package com.nivixx.ndatabase.api.query;

import java.util.ArrayList;
import java.util.List;

public class SingleNodePath {

    private String pathName;
    private SingleNodePath child;

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
