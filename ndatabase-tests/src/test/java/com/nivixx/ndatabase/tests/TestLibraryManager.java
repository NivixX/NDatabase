package com.nivixx.ndatabase.tests;

import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.URLClassLoaderHelper;
import net.byteflux.libby.logging.adapters.JDKLogAdapter;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.logging.Logger;

public class TestLibraryManager<T> extends LibraryManager {
    private final URLClassLoaderHelper classLoader;

    public TestLibraryManager(Logger logger, Path dataDirectory, T testClass) {
        super(new JDKLogAdapter(logger), dataDirectory, "test-dependencies");
        this.classLoader = new URLClassLoaderHelper(new URLClassLoader(new URL[0], testClass.getClass().getClassLoader()), this);
    }

    protected void addToClasspath(Path file) {
        this.classLoader.addToClasspath(file);
    }

}
