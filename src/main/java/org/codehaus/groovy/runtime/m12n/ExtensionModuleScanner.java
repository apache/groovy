/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.m12n;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.util.URLStreams;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A module extension scanner is responsible for searching classpath modules,
 * loading metadata from module descriptors, then perform custom tasks for each
 * module.
 *
 * This class was introduced as a fix for GROOVY-6008
 *
 * @since 2.1.2
 */
public class ExtensionModuleScanner {

    /**
     * Legacy location for META-INF module metadata files (Java services format).
     */
    public static final String LEGACY_MODULE_META_INF_FILE = "META-INF/services/org.codehaus.groovy.runtime.ExtensionModule";

    /**
     * Current location for META-INF module metadata files (Java properties format).
     */
    public static final String MODULE_META_INF_FILE = "META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule";

    private static final Logger LOG = Logger.getLogger(ExtensionModuleScanner.class.getName());

    /**
     * Modules whose load was deferred because a class named by their
     * {@code requiredClasses} could not be loaded, keyed by the scanning
     * class loader and then by module name.
     */
    private static final Map<ClassLoader, Map<String, Properties>> DEFERRED_MODULES = new WeakHashMap<>();

    private final ExtensionModuleListener listener;
    private final ClassLoader classLoader;

    /**
     * Constructs a new ExtensionModuleScanner.
     *
     * @param listener the listener to be notified when modules are discovered
     * @param loader the class loader to use for scanning and loading modules
     */
    public ExtensionModuleScanner(final ExtensionModuleListener listener, final ClassLoader loader) {
        this.listener = Objects.requireNonNull(listener);
        this.classLoader = Objects.requireNonNull(loader);
    }

    /**
     * Scans the classpath for extension module metadata files.
     * This method scans for both the current and legacy module metadata file locations.
     */
    public void scanClasspathModules() {
        scanClasspathModulesFrom(MODULE_META_INF_FILE);
        scanClasspathModulesFrom(LEGACY_MODULE_META_INF_FILE);
    }

    private void scanClasspathModulesFrom(final String moduleMetaInfFile) {
        try {
            for (URL url : DefaultGroovyMethods.toSet(classLoader.getResources(moduleMetaInfFile))) {
                scanExtensionModuleFromMetaInf(url);
            }
        } catch (final IOException e) {
            // DO NOTHING
        }
    }

    private void scanExtensionModuleFromMetaInf(final URL metadata) {
        Properties properties = new Properties();

        try (var stream = URLStreams.openUncachedStream(metadata)) {
            properties.load(stream);
        } catch (final IOException e) {
            throw new GroovyRuntimeException("Unable to load module META-INF descriptor", e);
        }

        scanExtensionModuleFromProperties(properties);
    }

    /**
     * Scans and creates an extension module from the given properties.
     * Properties are typically loaded from a module metadata file (properties format).
     *
     * @param properties the module metadata properties
     */
    public void scanExtensionModuleFromProperties(final Properties properties) {
        String moduleName = properties.getProperty(PropertiesModuleFactory.MODULE_NAME_KEY);
        if (moduleName != null) {
            String missingClass = missingRequiredClass(properties, definingLoader(properties, classLoader));
            if (missingClass != null) {
                deferModule(moduleName, properties, missingClass);
                return;
            }
            forgetDeferredModule(moduleName);
        }
        var factory = new StandardPropertiesModuleFactory();
        var module = factory.newModule(properties, classLoader);
        if (module instanceof SimpleExtensionModule simpleModule
                && simpleModule.getStaticMethodsExtensionClasses().isEmpty()
                && simpleModule.getInstanceMethodsExtensionClasses().isEmpty()) {
            module = null; // GROOVY-6491: empty module implies restriction (like OSGi)
        }
        if (module != null) listener.onModule(module);
    }

    /**
     * Returns the class loader that defines the module's extension classes, which
     * is the loader their signatures link against: a class a child loader can see
     * does not help an extension class defined by the parent. Falls back to the
     * scanning loader when no extension class can be loaded (the module factory
     * reports that case). Loading without initializing does not link the class.
     */
    private static ClassLoader definingLoader(final Properties properties, final ClassLoader loader) {
        for (String key : new String[]{MetaInfExtensionModule.MODULE_INSTANCE_CLASSES_KEY, MetaInfExtensionModule.MODULE_STATIC_CLASSES_KEY}) {
            for (String className : properties.getProperty(key, "").trim().split("[,; ]")) {
                className = className.trim();
                if (className.isEmpty()) continue;
                try {
                    ClassLoader definer = Class.forName(className, false, loader).getClassLoader();
                    return definer != null ? definer : loader;
                } catch (ClassNotFoundException | LinkageError ignore) {
                    // not loadable at all; the module factory logs it
                }
            }
        }
        return loader;
    }

    /**
     * Returns the first class named by the module's {@code requiredClasses} that
     * cannot be loaded from the given class loader, or null when all can be.
     */
    private static String missingRequiredClass(final Properties properties, final ClassLoader loader) {
        for (String className : properties.getProperty(MetaInfExtensionModule.MODULE_REQUIRED_CLASSES_KEY, "").trim().split("[,; ]")) {
            className = className.trim();
            if (className.isEmpty()) continue;
            try {
                Class.forName(className, false, loader);
            } catch (ClassNotFoundException | LinkageError e) {
                return className;
            }
        }
        return null;
    }

    private void deferModule(final String moduleName, final Properties properties, final String missingClass) {
        synchronized (DEFERRED_MODULES) {
            DEFERRED_MODULES.computeIfAbsent(classLoader, k -> new LinkedHashMap<>()).put(moduleName, properties);
        }
        LOG.log(Level.FINE, () -> "Module [" + moduleName + "] - load deferred until class " + missingClass + " is available to " + classLoader);
    }

    private void forgetDeferredModule(final String moduleName) {
        synchronized (DEFERRED_MODULES) {
            Map<String, Properties> deferred = DEFERRED_MODULES.get(classLoader);
            if (deferred != null && deferred.remove(moduleName) != null && deferred.isEmpty()) {
                DEFERRED_MODULES.remove(classLoader);
            }
        }
    }

    /**
     * Returns the names of the modules whose load was deferred for the given
     * class loader because a class named by their {@code requiredClasses}
     * could not be loaded.
     *
     * @param loader the class loader the modules were scanned with
     * @return the deferred module names, in scan order
     * @since 7.0.0
     */
    public static Set<String> getDeferredModuleNames(final ClassLoader loader) {
        synchronized (DEFERRED_MODULES) {
            Map<String, Properties> deferred = DEFERRED_MODULES.get(loader);
            return deferred == null ? Collections.emptySet() : new LinkedHashSet<>(deferred.keySet());
        }
    }

    /**
     * Removes and returns the deferred modules whose {@code requiredClasses}
     * can now all be loaded, so that the caller can register them with the
     * class loader they were scanned with. Grape calls this after it has added
     * grabbed jars to a class loader, in case one of them supplied a class an
     * already-scanned module was waiting for.
     *
     * @return the module descriptors now loadable, keyed by their scanning class loader
     * @since 7.0.0
     */
    public static Map<ClassLoader, List<Properties>> claimSatisfiedDeferredModules() {
        List<Map.Entry<ClassLoader, Properties>> candidates = new ArrayList<>();
        synchronized (DEFERRED_MODULES) {
            DEFERRED_MODULES.forEach((loader, modules) -> modules.values().forEach(module -> candidates.add(Map.entry(loader, module))));
        }
        // probe outside the lock: class loading may itself trigger a scan
        Map<ClassLoader, List<Properties>> satisfied = new LinkedHashMap<>();
        for (Map.Entry<ClassLoader, Properties> candidate : candidates) {
            ClassLoader scanningLoader = candidate.getKey();
            Properties properties = candidate.getValue();
            ClassLoader definer = definingLoader(properties, scanningLoader);
            String missingClass = missingRequiredClass(properties, definer);
            if (missingClass == null) {
                satisfied.computeIfAbsent(scanningLoader, k -> new ArrayList<>()).add(properties);
            } else if (definer != scanningLoader && missingRequiredClass(properties, scanningLoader) == null) {
                // the class arrived in a child loader (a plain @Grab from a script) and cannot be seen
                // from the loader that holds the module (the launcher's RootLoader for a jar in lib)
                LOG.warning(() -> "Module [" + properties.getProperty(PropertiesModuleFactory.MODULE_NAME_KEY) + "] - class " + missingClass
                        + " is visible to " + scanningLoader + " but not to " + definer + ", which defines the module's extension classes;"
                        + " the module stays deferred. A script can grab the dependency with @GrabConfig(systemClassLoader=true) instead.");
            }
        }
        synchronized (DEFERRED_MODULES) {
            satisfied.forEach((loader, modules) -> {
                Map<String, Properties> deferred = DEFERRED_MODULES.get(loader);
                if (deferred != null) {
                    modules.forEach(module -> deferred.remove(module.getProperty(PropertiesModuleFactory.MODULE_NAME_KEY)));
                    if (deferred.isEmpty()) DEFERRED_MODULES.remove(loader);
                }
            });
        }
        return satisfied;
    }

    /**
     * Listener interface for extension module discovery events.
     */
    public interface ExtensionModuleListener {
        /**
         * Called when an extension module is discovered and successfully loaded.
         *
         * @param module the discovered extension module
         */
        void onModule(ExtensionModule module);
    }
}
