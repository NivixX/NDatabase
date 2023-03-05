package com.nivixx.ndatabase.core.expressiontree;

import java.util.ArrayList;
import java.util.List;

public class MultiNodePath {

    private String pathName;
    private List<MultiNodePath> childs;

    public MultiNodePath() {
        this.pathName = "";
        this.childs = new ArrayList<>();
    }

    public void addChild(MultiNodePath child) {
        childs.add(child);
    }

    public List<MultiNodePath> getChilds() {
        return childs;
    }

    public void setChilds(List<MultiNodePath> childs) {
        this.childs = childs;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }


    public String getPathName() {
        return pathName;
    }

}
