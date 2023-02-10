package com.nivixx.ndatabase.core.config;

public class SqliteConfig {

    // eg: /home/server/plugins/NDatabase/mydatabase.sqlite
    private String fileFullPath;

    public String getFileFullPath() {
        return fileFullPath;
    }

    public void setFileFullPath(String fileFullPath) {
        this.fileFullPath = fileFullPath;
    }
}
