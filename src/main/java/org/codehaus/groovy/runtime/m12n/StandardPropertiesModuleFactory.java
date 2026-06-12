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

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * This is the standard Groovy module factory. This factory will build a module
 * using the {@link MetaInfExtensionModule} by default, unless a key named
 * "moduleFactory" is found in the properties file. If this is the case, then a new
 * factory is instantiated and used instead of this factory.
 */
public class StandardPropertiesModuleFactory extends PropertiesModuleFactory {

    /**
     * Property key in META-INF configuration file for specifying a custom module factory class.
     * If specified, the factory class must extend PropertiesModuleFactory and have a no-arg constructor.
     */
    public static final String MODULE_FACTORY_KEY = "moduleFactory";

    /**
     * Creates a new extension module from the given properties.
     * If the properties contain a custom factory class name under the key
     * {@link #MODULE_FACTORY_KEY}, that factory is instantiated and used.
     * Otherwise, a {@link MetaInfExtensionModule} is created.
     *
     * @param properties the module metadata properties
     * @param classLoader the class loader to use for loading extension classes and custom factory classes
     * @return a new extension module
     * @throws GroovyRuntimeException if the custom factory cannot be loaded or instantiated
     */
    @Override
    @SuppressWarnings("unchecked")
    public ExtensionModule newModule(final Properties properties, final ClassLoader classLoader) {
        String factoryName = properties.getProperty(MODULE_FACTORY_KEY);
        if (factoryName != null) {
            try {
                var factoryClass = (Class<? extends PropertiesModuleFactory>) classLoader.loadClass(factoryName);
                PropertiesModuleFactory delegate = factoryClass.getDeclaredConstructor().newInstance();
                return delegate.newModule(properties, classLoader);
            } catch (final ClassNotFoundException | NoSuchMethodException e) {
                throw new GroovyRuntimeException("Unable to load module factory [" + factoryName + "]", e);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new GroovyRuntimeException("Unable to instantiate module factory [" + factoryName + "]", e);
            }
        }

        return MetaInfExtensionModule.newModule(properties, classLoader);
    }
}
