package com.nivixx.ndatabase.platforms.spongeplatform;

import com.google.inject.Inject;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.URLClassLoaderHelper;
import net.byteflux.libby.logging.adapters.SpongeLogAdapter;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.config.ConfigDir;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;

public class CustomSpongeLibraryManager<T> extends LibraryManager {
    private final URLClassLoaderHelper classLoader;

    public CustomSpongeLibraryManager(Logger logger, @ConfigDir(sharedRoot = false) Path dataDirectory, T plugin, String directoryName) {
        super(new SpongeLogAdapter(logger), dataDirectory, directoryName);
        this.classLoader = new URLClassLoaderHelper(new URLClassLoader(new URL[0], plugin.getClass().getClassLoader()), this);
    }

    protected void addToClasspath(Path file) {
        this.classLoader.addToClasspath(file);
    }

}
