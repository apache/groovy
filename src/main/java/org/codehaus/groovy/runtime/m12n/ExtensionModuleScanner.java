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
import java.util.Objects;
import java.util.Properties;

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
     * @deprecated This location is maintained for backward compatibility. Prefer {@link #MODULE_META_INF_FILE}.
     */
    public static final String LEGACY_MODULE_META_INF_FILE = "META-INF/services/org.codehaus.groovy.runtime.ExtensionModule";

    /**
     * Current location for META-INF module metadata files (Java properties format).
     */
    public static final String MODULE_META_INF_FILE = "META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule";

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
