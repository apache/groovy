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

import java.lang.reflect.InaccessibleObjectException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toList;

/**
 * A {@link SimpleExtensionModule} implementation which reads extension class
 * metadata from META-INF.
 *
 * @since 2.0.0
 */
@SuppressWarnings("rawtypes")
public class MetaInfExtensionModule extends SimpleExtensionModule {

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

    //-----------------------------------------------------------------------

    public static final String MODULE_INSTANCE_CLASSES_KEY = "extensionClasses";
    public static final String MODULE_STATIC_CLASSES_KEY   = "staticExtensionClasses";

    public static MetaInfExtensionModule newModule(final Properties properties, final ClassLoader loader) {
        String name = properties.getProperty(PropertiesModuleFactory.MODULE_NAME_KEY);
        if (name == null) {
            throw new GroovyRuntimeException("Module file has not set the module name using key: " + PropertiesModuleFactory.MODULE_NAME_KEY);
        }
        String version = properties.getProperty(PropertiesModuleFactory.MODULE_VERSION_KEY);
        if (version == null) {
            throw new GroovyRuntimeException("Module file has not set the module version using key: " + PropertiesModuleFactory.MODULE_VERSION_KEY);
        }

        Function<String, Class> load = (extensionClass) -> {
            try {
                extensionClass = extensionClass.trim();
                if (!extensionClass.isEmpty()) {
                    return loader.loadClass(extensionClass);
                }
            } catch (ClassNotFoundException | InaccessibleObjectException | LinkageError error) {
                var logger = java.util.logging.Logger.getLogger(MetaInfExtensionModule.class.getName());
                logger.log(WARNING, "Module [" + name + "] - Unable to load extension class: " + extensionClass, error);
            }
            return null;
        };

        String[] objectExtensionClasses = properties.getProperty(MODULE_INSTANCE_CLASSES_KEY, "").trim().split("[,; ]");
        String[] staticExtensionClasses = properties.getProperty(  MODULE_STATIC_CLASSES_KEY, "").trim().split("[,; ]");

        List<Class> objectClasses = Stream.of(objectExtensionClasses).map(load).filter(Objects::nonNull).collect(toList());
        List<Class> staticClasses = Stream.of(staticExtensionClasses).map(load).filter(Objects::nonNull).collect(toList());

        return new MetaInfExtensionModule(name, version, objectClasses, staticClasses);
    }
}
