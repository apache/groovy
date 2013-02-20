/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.m12n;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly;

/**
 * A module extension scanner is responsible for searching classpath modules,
 * loading metadata from module descriptors, then perform custom tasks for each
 * module.
 *
 * This class was introduced as a fix for GROOVY-6008
 *
 * @author Cedric Champeau
 * @since 2.1.2
 */
public class ExtensionModuleScanner {
    public final static String MODULE_META_INF_FILE = "META-INF/services/org.codehaus.groovy.runtime.ExtensionModule";

    private final ExtensionModuleListener listener;
    private final ClassLoader classLoader;

    public ExtensionModuleScanner(final ExtensionModuleListener listener, final ClassLoader loader) {
        this.listener = listener;
        this.classLoader = loader;
    }

    public void scanClasspathModules() {
        try {
            Enumeration<URL> resources = classLoader.getResources(MODULE_META_INF_FILE);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                scanExtensionModuleFromMetaInf(url);
            }
        } catch (IOException e) {
            // DO NOTHING
        }
    }

    private void scanExtensionModuleFromMetaInf(final URL metadata) {
        Properties properties = new Properties();
        InputStream inStream = null;
        try {
            inStream = metadata.openStream();
            properties.load(inStream);
        } catch (IOException e) {
            throw new GroovyRuntimeException("Unable to load module META-INF descriptor", e);
        } finally {
            closeQuietly(inStream);
        }
        scanExtensionModuleFromProperties(properties);
    }

    public void scanExtensionModuleFromProperties(final Properties properties) {
        StandardPropertiesModuleFactory factory = new StandardPropertiesModuleFactory();
        ExtensionModule module = factory.newModule(properties, classLoader);
        listener.onModule(module);
    }


    public static interface ExtensionModuleListener {
        void onModule(ExtensionModule module);
    }
}
