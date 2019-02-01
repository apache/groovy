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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A {@link SimpleExtensionModule} implementation which reads extension classes
 * metadata from META-INF.
 *
 * @since 2.0.0
 */
public class MetaInfExtensionModule extends SimpleExtensionModule {
    private static final Logger LOG = Logger.getLogger(MetaInfExtensionModule.class.getName());

    public static final String MODULE_INSTANCE_CLASSES_KEY = "extensionClasses";
    public static final String MODULE_STATIC_CLASSES_KEY = "staticExtensionClasses";

    private final List<Class> instanceExtensionClasses;
    private final List<Class> staticExtensionClasses;

    @Override
    public List<Class> getInstanceMethodsExtensionClasses() {
        return instanceExtensionClasses;
    }

    @Override
    public List<Class> getStaticMethodsExtensionClasses() {
        return staticExtensionClasses;
    }

    private MetaInfExtensionModule(final String moduleName, final String moduleVersion, final List<Class> instanceExtensionClasses, final List<Class> staticExtensionClasses) {
        super(moduleName, moduleVersion);
        this.instanceExtensionClasses = instanceExtensionClasses;
        this.staticExtensionClasses = staticExtensionClasses;
    }

    public static MetaInfExtensionModule newModule(final Properties properties, final ClassLoader loader) {
        String name = properties.getProperty(PropertiesModuleFactory.MODULE_NAME_KEY);
        if (name == null)
            throw new GroovyRuntimeException("Module file hasn't set the module name using key [" + PropertiesModuleFactory.MODULE_NAME_KEY + "]");
        String version = properties.getProperty(PropertiesModuleFactory.MODULE_VERSION_KEY);
        if (version == null)
            throw new GroovyRuntimeException("Module file hasn't set the module version using key [" + PropertiesModuleFactory.MODULE_VERSION_KEY + "]");
        String[] extensionClasses = properties.getProperty(MODULE_INSTANCE_CLASSES_KEY, "").trim().split("[,; ]");
        String[] staticExtensionClasses = properties.getProperty(MODULE_STATIC_CLASSES_KEY, "").trim().split("[,; ]");
        List<Class> instanceClasses = new ArrayList<Class>(extensionClasses.length);
        List<Class> staticClasses = new ArrayList<Class>(staticExtensionClasses.length);
        List<String> errors = new LinkedList<String>();
        loadExtensionClass(loader, extensionClasses, instanceClasses, errors);
        loadExtensionClass(loader, staticExtensionClasses, staticClasses, errors);
        if (!errors.isEmpty()) {
            for (String error : errors) {
                LOG.warning("Module [" + name + "] - Unable to load extension class [" + error + "]");
            }
        }
        return new MetaInfExtensionModule(name, version, instanceClasses, staticClasses);
    }

    private static void loadExtensionClass(ClassLoader loader, String[] extensionClasses, List<Class> instanceClasses, List<String> errors) {
        for (String extensionClass : extensionClasses) {
            try {
                extensionClass = extensionClass.trim();
                if (extensionClass.length() > 0) {
                    instanceClasses.add(loader.loadClass(extensionClass));
                }
            } catch (ClassNotFoundException | NoClassDefFoundError | UnsupportedClassVersionError e) {
                errors.add(extensionClass);
            }
        }
    }
}
